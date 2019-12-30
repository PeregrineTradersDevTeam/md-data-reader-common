package com.peregrinetraders.beans;

public class OutputRecord<T> {
	private long packetTimestamp;
	private long marketTimestamp;
	private T topRecord;
	
	public OutputRecord(long packetTimestamp, long marketTimestamp, T topRecord) {
		super();
		this.packetTimestamp = packetTimestamp;
		this.marketTimestamp = marketTimestamp;
		this.topRecord = topRecord;
	}

	public long getPacketTimestamp() {
		return packetTimestamp;
	}

	public long getMarketTimestamp() {
		return marketTimestamp;
	}

	public T getTopRecord() {
		return topRecord;
	}
	
}
