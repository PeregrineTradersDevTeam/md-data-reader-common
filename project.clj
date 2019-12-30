(defproject pcap_common "0.1.0-SNAPSHOT"
  :description "common base for ETL applications with sources from Pico data"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :java-source-paths ["src/java"]
  :source-paths ["src/clj"]
  :plugins [[s3-wagon-private "1.3.2"]]
  :repositories [["releases" {:url "s3p://md-data-reader-artifacts/releases/" :no-auth true}]
                 ["snapshots" {:url "s3p://md-data-reader-artifacts/snapshots" :no-auth true}]]
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [byte-streams "0.2.4"]
                 [byte-streams "0.2.4"]
                 [org.clojure/tools.cli "0.4.2"]
                 [cheshire "5.8.1"]
                 [org.agrona/agrona "0.9.35"]
 		 [io.kaitai/kaitai-struct-runtime "0.8"]
                 [org.clojure/data.int-map "0.2.4"]
                 [org.apache.parquet/parquet-common "1.10.1"]
                 [org.apache.parquet/parquet-column "1.10.1"]
                 [org.apache.parquet/parquet-encoding "1.10.1"]
                 [org.apache.parquet/parquet-hadoop "1.10.1"]
                 [org.apache.hadoop/hadoop-core "1.2.1"]
                 ])
