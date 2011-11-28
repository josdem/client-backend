package com.all.rds.service;

public interface ChunkStorageService {

	boolean put(String trackId, int chunkId, byte[] chunk);

	byte[] get(String trackId, int chunkId);

}
