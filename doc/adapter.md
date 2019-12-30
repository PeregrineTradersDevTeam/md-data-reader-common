# Adapting to Parquet
There are 3 aspects to adaptation:

1. The general Parquet writing API
2. The master adapter
3. Clojure inner adapter design


## Parquet low-level API
The low-level Java Parquet API starts with org.apache.parquet.hadoop.ParquetRecordWriter<T>, where T is the object to be written.
Please note that a specific writer can only write the same type of object and, transitively, support only one schema.

The actual writing logic is handled via org.apache.parquet.hadoop.api.WriteSupport<T>.

An extension of the class can then physically write to Parquet by means of a org.apache.parquet.io.api.RecordConsumer.

## The master adapter
The responsibilities of the master adapter are:

1. Performing the role of a Java Stream Sink, being given a BufferAndOffset.
2. Instantiate individual ParquetRecordWriter's for each message type, and dispatch it to the corresponding adapter
3. Provide Parquet adapters.

#### Anatomy of a master adapter
 The common repository provides:
 
```
MasterDriver/masterStream(
			List<File> pcapFiles, 
			Function<PacketBuffer, Stream<BufferAndOffset>> frameDecoder, 
			Consumer<BufferAndOffset> sink)
```
The frame decoder is implemented by each main project, in Java.

So all the main program would do is call this method with the instantiated frameDecoder and a sink.

A minimal implementation of the Sink would be java.util.function.Consumer<BufferAndOffset> which, in Clojure, looks like this [1]:

```
(jconsumer [^BufferAndOffset info]
                           (let [buffer        (.getBuffer info)
                                 offset        (.getOffset info)
                                 header        (.wrap (MessageHeaderDecoder.) buffer offset)
                                 templateId    (.templateId header)]
                            (println templateId))) 
```

In Clojure, resource handling is handled via an RAII-like pattern, so the main adapter is structured as following:
```
	(let [... instantiate ParquetRecordWriter's ...
		  sink-definition-using-writers (... as above... )
		  project-specific-frame-decoder (new ...)]
	(try 
	 (MasterDriver/masterStream pcap_files project-specific-frame-decoder sink-definition-using-writers)
	 (finally
	  ... clean up record writers ...)))
```



#### Parquet Adapter design
By now, it should be clear that all there is to it is implementing the WriteSupport interface. Several requirements impact the structure of the adapter:

1. There have to be a set of fields (market time, packet) time that must be present in each adapter.
2. Business logic needs to be inlined for maximum performance.
3. The RecordConsumer API is extremely verbose & repetitive, adapter code should be simplified for maintenance.

All of the support code is in ```src/clj/pcap_common/macros.clj``` and ```src/clj/pcap_common/parquet_util.clj```

```parquet_util``` handles assembling a proper ParquetWriter from the various components. It also contains 2 basic implementations of WriteSupport, ```make-loopful-proxy``` and ```make-loopless-proxy```. The latter implements a flattened adaptation for use in Euronext. The former should be considered the basic implementation.

Both functions maintain the invariant that there is a top-level schema containing the timestamps, and that they are properly adapted from the OutputRecord. Afterwards, they call their ```msg-write-fn``` parameter to complete the actual SBE->Parquet adaptation.

Looking at the Java example for the RecordConsumer documentation:

```
  startField("A", 0)
   addValue(1)
   addValue(2)
  endField("A", 0)
```

We can see it's quite verbose to add a single field. There is thus, a Clojure macro, ```with-field-new```, which helps reduce the repetitiveness, while also inlining. The Clojure version looks like:

```
(with-field-new rconsumer "arrival_time" 0 (.addLong (.getPacketTimestamp outer-record)))
```


[1] JConsumer is a macro simplifying Clojure's way of implementing an interface
