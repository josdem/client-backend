package com.all.rds.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity
public class CachedTrackChunk {

	@SuppressWarnings("unused")
	@Id
	private String id;

	@Lob
	private byte[] data;

	public CachedTrackChunk() {
	}

	public CachedTrackChunk(String trackId, int chunkNumber) {
		this.id = createChunkId(trackId, chunkNumber);
	}

	public CachedTrackChunk(String trackId, int chunkNumber, byte[] data) {
		this(trackId, chunkNumber);
		this.data = data;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public static String createChunkId(String trackId, int chunkNumber) {
		return trackId + "_" + chunkNumber;
	}
}
