(ns pcap-common.parquet-util
  (:use [clojure.java.io :only [file]]
        [pcap-common.macros])
  (:import [org.apache.hadoop.fs Path]
           [java.util Map]
           [com.peregrinetraders.beans OutputRecord]
           [org.apache.parquet.schema MessageTypeParser MessageType]
           [org.apache.parquet.hadoop ParquetOutputFormat]
           [org.apache.parquet.hadoop.metadata CompressionCodecName]
           [org.apache.parquet.hadoop.api WriteSupport WriteSupport$WriteContext]
           [org.apache.parquet.io.api Binary RecordConsumer]
           [org.apache.hadoop.conf Configuration]))

(defn make-test-conf []
  (doto (Configuration.)
    (.setLong "parquet.block.size" (* 1  52428800))
    (.setInt "parquet.page.size" (* 1  1048576))
    (.setBoolean "parquet.enable.dictionary" false)
    (.setLong "parquet.memory.min.chunk.size" (* 1))))

(defn compile-schema [message-schema-string]
  (MessageTypeParser/parseMessageType
   (str "message root {
optional int64 arrival_time;
optional int64 packet_time;
" message-schema-string
        "}")))

(defn make-loopless-proxy [msg-write-fn ^MessageType schema meta]
    {:pre [(instance? Map meta) schema]}
  (let [record-consumer-state (atom nil)]

    (proxy [WriteSupport] []
      (init [_]
        (WriteSupport$WriteContext. schema ^Map meta))
      (prepareForWrite [record-consumer]
        (reset! record-consumer-state record-consumer))
      (write [^OutputRecord outer-record]
        (let [^RecordConsumer rconsumer @record-consumer-state]
          (.startMessage rconsumer)
          (with-field-new rconsumer "arrival_time" 0 (.addLong (.getPacketTimestamp outer-record)))
          (with-field-new rconsumer "packet_time" 1 (.addLong (.getMarketTimestamp outer-record)))
          (msg-write-fn rconsumer 2 (.getTopRecord outer-record))
          (.endMessage rconsumer))))))

(defn make-loopfull-proxy [msg-write-fn repetition-fn ^MessageType schema meta repetition-count]
  {:pre [(instance? Map meta) schema]}
  (let [record-consumer-state (atom nil)]

    (proxy [WriteSupport] []
      (init [_]
        (WriteSupport$WriteContext. schema ^Map meta))
      (prepareForWrite [record-consumer]
        (reset! record-consumer-state record-consumer))
      (write [^OutputRecord outer-record]
        (let [^RecordConsumer rconsumer @record-consumer-state
              top-record (.getTopRecord outer-record)]
          (let [repetition (repetition-fn top-record)]
            (when (> (repetition-count repetition) 0)
              (while (.hasNext ^java.util.Iterator repetition)
                (let [group-rep (.next ^java.util.Iterator repetition)]
                  (.startMessage rconsumer)
                  (with-field-new rconsumer "packet_timestamp" 0 (.addLong (.getPacketTimestamp outer-record)))
                  (with-field-new rconsumer "market_timestamp" 1 (.addLong (.getMarketTimestamp outer-record)))
                  (msg-write-fn rconsumer 2 top-record group-rep)
                  (.endMessage rconsumer)))
              )))))))

(defn make-parquet-integrated-writer [output_name output-format]
  (.getRecordWriter
   (ParquetOutputFormat. output-format)
   (make-test-conf)
   (Path. (.getAbsolutePath (file output_name)))
   CompressionCodecName/GZIP))
