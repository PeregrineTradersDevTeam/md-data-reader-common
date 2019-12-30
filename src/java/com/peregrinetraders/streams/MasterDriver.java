package com.peregrinetraders.streams;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import com.peregrinetraders.beans.BufferAndOffset;
import com.peregrinetraders.beans.PacketBuffer;
import com.struct.kaitai.Pcap;

public class MasterDriver {

	public static Stream<BufferAndOffset> sbeStream(Pcap pcap, Function<PacketBuffer, Stream<BufferAndOffset>> frameDecoder) {
		return 	Stream.of(pcap)
				.filter(f -> f != null)
				.flatMap(PcapGenerator::loadPackets)
				.map(PcapGenerator::extractFrames)
				.flatMap(frameDecoder);
	}
	public static void masterStream(
			List<File> pcapFiles, 
			Function<PacketBuffer, Stream<BufferAndOffset>> frameDecoder, 
			Consumer<BufferAndOffset> sink) throws IOException {
		for (File pcapFile : pcapFiles) {
			System.out.println(pcapFile.getAbsolutePath());
			Pcap pcap = PcapGenerator.loadGzippedPcapFile(pcapFile);
			sbeStream(pcap, frameDecoder).forEachOrdered(sink);
		}
	}
}
