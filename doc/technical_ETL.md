# Technical ETL view
Illustrations for the layers:

 - ![PCAP](pcap_exploration.png)
 - [How the payload looks like, further](blob:https://peregrinetraders.atlassian.net/98002993-367a-466e-afda-3d97934c537b#media-blob-url=true&id=4066f90b-6c89-43e3-9b76-8eb70e90c0b8&collection=contentId-163086390&contextId=163086390&mimeType=image%2Fpng&name=market_data_packet.png&size=47836&width=1458&height=616)

 
The program represents one big loop that performs the following:

 - Stream file via GZIP decompression filter
 - instantiate [KaitaiStruct](kaitai_struct.md) and stream PcapPacket's
 - for a packet, extract innermost datagram body - represents the binary Exchange layer
 - loop through the Exchange layer to extract SBE payloads
 - feed each SBE payload into the main Adapter
 
 
Streams are defined in src/java/com/peregrinetraders/streams. The basic workflow is:

```
 List<File>					 	the input source paths
 V								GUnzipped Pcap decoding
 Stream<Pcap>
 V 								Individual packet extraction
 Stream<Packet>
 V								Pcap Envelope stripping
 Stream<PacketFrames> 
 V								frame decoded according to exchange’s specifications
 V								stream flattening
 Stream<BufferAndOffset> 
 V								each individual message processed by the SBE->Parquet adapter 
 Parquet
```

Each application has to provide:
 - the source files
 - the frame decoder
 - the SBE->Parquet Sink
 
### Frame decoder
 It is just:
 ```Function<PacketBuffer, Stream<BufferAndOffset>>```.

 The basis for both input and output is an UnsafeBuffer, the binary payload. The input PacketBuffer represents the innermost PCAP-body, which is the Exchange's envelope. Each resulting BufferAndOffset is a reference to the same UnsafeBuffer, the starting point of the SBE payload

### SBE->Parquet Sink
 Simply:
```
Consumer<BufferAndOffset>
```
 This is provided by each main project (md-data-reader-euronext, md-data-reader-cme), and is covered in the [SBE workflow](sbe_workflow.md)