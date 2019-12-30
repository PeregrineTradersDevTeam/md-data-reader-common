package com.peregrinetraders.streams;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;

import org.agrona.concurrent.UnsafeBuffer;
import org.apache.commons.io.IOUtils;

import com.peregrinetraders.beans.PacketBuffer;
import com.struct.kaitai.EthernetFrame;
import com.struct.kaitai.Ipv4Packet;
import com.struct.kaitai.Pcap;
import com.struct.kaitai.Pcap.Packet;
import com.struct.kaitai.Pcap.PacketEntries;
import com.struct.kaitai.UdpDatagram;

import io.kaitai.struct.ByteBufferKaitaiStream;
import io.kaitai.struct.RandomAccessFileKaitaiStream;

public class PcapGenerator {
	
	public static Pcap loadGzippedPcapFile(File file) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (final FileInputStream fis = new FileInputStream(file);
				final GZIPInputStream gzis = new GZIPInputStream(fis))
		{
			IOUtils.copy(gzis, os);
			return new Pcap(new ByteBufferKaitaiStream(os.toByteArray()));
		} catch (OutOfMemoryError err) { // cannot allocate that much memory, stream to file as fallback
			System.out.println("Couldn't process" + file.getAbsolutePath() + " in memory; falling back to file");
			File fallback = File.createTempFile("tmp", file.getName());
			System.out.println(fallback.getAbsolutePath());
			fallback.deleteOnExit(); // praise be the JVM
			try (
					FileOutputStream fos = new FileOutputStream(fallback);
					final FileInputStream fis = new FileInputStream(file);
					final GZIPInputStream gzis = new GZIPInputStream(fis))
			{
				IOUtils.copy(gzis, fos);
				return new Pcap(new RandomAccessFileKaitaiStream(fallback.getAbsolutePath()));
			}
		}
	}
	
	public static class PacketIterator implements Iterator<Packet> {

		private PacketEntries entries;
		public PacketIterator(Pcap packets) {
			entries = packets.packets();
		}
		@Override
		public boolean hasNext() {
			return entries != null;
		}

		@Override
		public Packet next() {
			Packet entry = entries.entry();
			ArrayList<PacketEntries> nextEntriesList = entries.nextEntries();
			if (nextEntriesList.isEmpty()) {
				entries = null;
			} else {
				entries = nextEntriesList.get(0);
				// NB: this is important, as KaitaiStruct keeps back references to objects,
				// so we must clear the list in order to allow GC to claim them
				nextEntriesList.clear();
			}
			return entry;
		}
	}

	public static class PacketIterable implements Iterable<Packet> {
		private Pcap packets;
		public PacketIterable(Pcap packets) {
			this.packets = packets;
		}
		@Override
		public Iterator<Packet> iterator() {
			return new PacketIterator(packets);
		}
	}
	
	public static Stream<Packet> loadPackets(Pcap pcap) {
		return StreamSupport.stream(new PacketIterable(pcap).spliterator(), false);
	}
	
	public static PacketBuffer extractFrames(Packet p) {
		return new PacketBuffer(
				new UnsafeBuffer(((UdpDatagram)((Ipv4Packet)((EthernetFrame)p.body()).body()).body()).body()),
				(p.tsSec() * 1000000 + p.tsUsec())
				);
	}
}
