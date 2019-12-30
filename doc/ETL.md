# Abstract ETL view
The simplest description of the project is to take source exchange files and generate target Parquet datasets. Most of the complexity is in the transformation itself.

An input file represents the Pico format of delivering historical network data. It’s a gzip file containing a dump in the [pcap format](https://wiki.wireshark.org/Development/LibpcapFileFormat) [1]


Each file can be described as having 3 onion-like layers:

- the Pcap layer: contains the all of the various network envelopes (Ethernet, UDP, etc.). The innermost body represents
- the Exchange layer: usually a simple encoding to allow for multiple messages within a single datagram; each message is part of
- the SBE layer: an individual message is an SBE-encoded trade-specific message.

The application extracts all 3 layers, taking:

- the pcap's arrival time from the Pcap layer
- the market time from the Exchange layer
- all trade-specific information from the SBE layer

It then writes the information to parquet datasets representing each message type.

Since the input files represent the actual network traffic, they contain intermingled trade-specific messages.

Each trade message type has its own schema, and corresponds to a single parquet dataset.
 
More information can be found in the [Technical ETL View](technical_ETL.md).
 
[1] For Pico, the header has a different endianness than what local linux tcpdump produces

