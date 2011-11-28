package com.all.rds.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.all.rds.model.CachedTrackChunk;
import com.all.rds.service.ChunkStorageService;

@Service
public class ChunksDBStorageService implements ChunkStorageService {
	private static final byte[] EMPTY_CHUNK = new byte[] {};

	@Autowired
	private HibernateTemplate ht;

	@Override
	public boolean put(String trackId, int chunkId, byte[] chunk) {
		ht.save(new CachedTrackChunk(trackId, chunkId, chunk));
		return true;
	}

	@Override
	public byte[] get(String trackId, int chunkId) {
		CachedTrackChunk chunk = ht.get(CachedTrackChunk.class, CachedTrackChunk.createChunkId(trackId, chunkId));
		return chunk != null ? chunk.getData() : EMPTY_CHUNK;
	}

}
