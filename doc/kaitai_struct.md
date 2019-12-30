# KaitaiStruct

[KaitaiStruct](https://kaitai.io) is a declarative way to develop binary parsers.
 
 It could, in theory, define the same logic as SBE tools (who knows, maybe they share some ancestry?).
 
 It is used here to interpret the Pcap file format. Two reasons:
1. Like SBE, it abstracts away the error-prone binary manipulations into nice Java getters
2. There’s already a standard binary definition of the PCAP protocol!

 A .ksy file describes the binary PCAP format. A code generator generates Java files which encapsulate the binary manipulation.
 
 Unfortunately, the code does not generate any streaming-friendly format. As such, some manual editing had to happen in the generated Java code, in order to support the streaming case.
 
 The generated & modified code is present in `src/java/com/struct/kaitai`. The entry point is `Pcap.java`.
 
 The entirety of manipulation is encapsulated in PcapGenerator.java. This module's responsibility is to publish a Stream of buffers, from the outer binary data. It does this by adapting the Pcap Object into a Stream<Packet>, and for each packet, extracting the inner PacketBuffer by peeling off the network layers ![network layers](pcap_exploration.png). 
