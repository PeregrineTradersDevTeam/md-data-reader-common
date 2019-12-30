package com.peregrinetraders.beans;

import org.agrona.concurrent.UnsafeBuffer;

public class PacketBuffer {
	private UnsafeBuffer buffer;
	private long packetTime;
	public PacketBuffer(UnsafeBuffer buffer, long packetTime) {
		super();
		this.buffer = buffer;
		this.packetTime = packetTime;
	}
	public UnsafeBuffer getBuffer() {
		return buffer;
	}
	public void setBuffer(UnsafeBuffer buffer) {
		this.buffer = buffer;
	}
	public long getPacketTime() {
		return packetTime;
	}
	public void setPacketTime(long packetTime) {
		this.packetTime = packetTime;
	}
	
}
