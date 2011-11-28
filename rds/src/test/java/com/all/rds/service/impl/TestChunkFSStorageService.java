package com.all.rds.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.all.testing.MockInyectRunner;
import com.all.testing.Stub;
import com.all.testing.UnderTest;

@RunWith(MockInyectRunner.class)
public class TestChunkFSStorageService {

	private static final String TESTING_REST_SERVER = "testing rest server";
	private static final String ALL_CHUNKS = "./Chunks/";
	@UnderTest
	private ChunksFSStorageService service;
	@SuppressWarnings("unused")
	// injected
	@Stub
	private String homeDir = ALL_CHUNKS;
	private byte[] bytes;
	private final String HASHCODE = "66";

	@Before
	public void setup() {
		bytes = TESTING_REST_SERVER.getBytes();
	}

	@After
	public void teardown() {
		File file = new File(ALL_CHUNKS + HASHCODE);
		if (file.exists()) {
			for (File item : file.listFiles()) {
				item.delete();
			}
			file.delete();
			assertFalse(file.exists());
			file = new File(ALL_CHUNKS);
			file.delete();
			assertFalse(file.exists());
		}
	}

	@Test
	public void shouldputs() throws Exception {
		assertTrue(service.put(HASHCODE, 0, bytes));
	}

	@Test
	public void shouldNotWriteDuplicateChunks() throws Exception {
		assertTrue(service.put(HASHCODE, 10, bytes));
		assertFalse(service.put(HASHCODE, 10, bytes));
	}

	@Test
	public void shouldGetOneChunk() throws Exception {
		assertTrue(service.put(HASHCODE, 15, bytes));
		String result = new String(service.get(HASHCODE, 15));
		assertEquals(TESTING_REST_SERVER, result);
	}

	@Test
	public void shouldWrite100ChunksAndGetAllBack() throws Exception {
		for (int i = 0; i < 100; i++) {
			assertTrue(service.put(HASHCODE, i, bytes));
		}
		File file = new File(ALL_CHUNKS + HASHCODE);
		assertEquals(100, file.listFiles().length);

		for (int i = 0; i < 100; i++) {
			String result = new String(service.get(HASHCODE, i));
			assertEquals(TESTING_REST_SERVER, result);
		}
	}

	@Test
	public void shouldNotFindAChunk() throws Exception {
		assertTrue(service.put(HASHCODE, 15, bytes));
		byte[] bytes = service.get(HASHCODE, 16);
		assertTrue(bytes.length == 0);
	}

}
