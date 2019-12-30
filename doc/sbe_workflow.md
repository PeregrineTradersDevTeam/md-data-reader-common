# SBE workflow

The SBE framework is a mature, (almost…) well designed project to support binary market formats.
 
The principle is to represent messages as binary payloads, with an XML description of their overall structure.
 
The framework provides tools to generate Java parsing code from the XML. There are 2 modes of execution:

1. Interpreted mode: the software generates special bytecode from the XML, and then uses that bytecode at runtime in order to walk the binary payload
2. Hardcoded mode: the software generates Java classes which encapsulate direct buffer access in the form of getters/setters.
 
 
The second mode is the fastest possible, as the Java compiler should inline the code to the point of simple memory copies. The downside is that it cannot switch XML implementations at runtime. As such, it is up to the application code to generate new Java classes, integrate them, and build a new JAR file.
 
Generated classes are to be found in each exchange's project (md-data-reader-euronext, md-data-reader-cme).

### Explaining the (almost...)
 While the usage of generated classes is straightforward, an important mention is the usage of multiple repeated groups.
 
 In the main Decoder, repetitions are handled by inner classes (also called Decoder). However, the handling is *not* random access. Contrast this to regular getters, which are just getting data from the buffers at specific offsets.
 
 The main decoder has a limit member, that represents the "end" of its data. When instantiation an inner decoder, the following happens:
 
 ```
 final int limit = parentMessage.limit();
 parentMessage.limit(limit + HEADER_SIZE);
 ```
 
 The "end" of the message changes!

 Worse, when moving the inner decoder's Iterator:
 
 ```
 offset = parentMessage.limit();
 parentMessage.limit(offset + blockLength);
 ```
 
 So, the inner groups treat the end of the buffer as some sort of pointer, incrementing it by their own block size.
 
 This efffectively means groups are **NOT** random access, and **STATEFUL**.
 
 In order to succesfully work with the file, groups need to be accessed in the **SAME** order as defined in the XML schema.
 
### Further processing
 Once an SBE Header is instantiated, the main program can then dispatch to a specific decoder, instantiate that in turn, and finally feed into an output [adapter function](adapter.md)
