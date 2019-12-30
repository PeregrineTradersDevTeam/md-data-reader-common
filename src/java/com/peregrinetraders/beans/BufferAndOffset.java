package com.peregrinetraders.beans;

import org.agrona.concurrent.UnsafeBuffer;

public class BufferAndOffset {
	private UnsafeBuffer buffer;
	private long offset;
	private long packetTime;
	private long marketTime;
	

	public BufferAndOffset(UnsafeBuffer buffer, long offset, long packetTime, long marketTime) {
		super();
		this.buffer = buffer;
		this.offset = offset;
		this.packetTime = packetTime;
		this.marketTime = marketTime;
	}
	public UnsafeBuffer getBuffer() {
		return buffer;
	}
	public void setBuffer(UnsafeBuffer buffer) {
		this.buffer = buffer;
	}
	public long getOffset() {
		return offset;
	}
	public void setOffset(long offset) {
		this.offset = offset;
	}
	public long getPacketTime() {
		return packetTime;
	}
	public void setPacketTime(long packetTime) {
		this.packetTime = packetTime;
	}
	public long getMarketTime() {
		return marketTime;
	}
	public void setMarketTime(long marketTime) {
		this.marketTime = marketTime;
	}
	
}
