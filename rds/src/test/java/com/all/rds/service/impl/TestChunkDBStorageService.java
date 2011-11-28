package com.all.rds.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.all.rds.model.CachedTrackChunk;
import com.all.rds.service.ChunkStorageService;

public class TestChunkDBStorageService {

	@InjectMocks
	private ChunkStorageService service = new ChunksDBStorageService();
	@Mock
	private HibernateTemplate ht;
	@Captor
	private ArgumentCaptor<CachedTrackChunk> chunkCaptor;

	private int chunkId = 0;
	private String trackId = "trackId";

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldGetChunkFromDb() throws Exception {
		service.get(trackId, chunkId);

		verify(ht).get(CachedTrackChunk.class, CachedTrackChunk.createChunkId(trackId, chunkId));
	}

	@Test
	public void shouldStoreChunk() throws Exception {
		byte[] chunk = new byte[] { 0, 1, 2, 3 };
		service.put(trackId, chunkId, chunk);

		verify(ht).save(chunkCaptor.capture());
		CachedTrackChunk storedChunk = chunkCaptor.getValue();
		assertNotNull(storedChunk);
		assertTrue(Arrays.equals(chunk, storedChunk.getData()));
	}

}
