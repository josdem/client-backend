package com.all.rds.loader.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Chunker {
	private static final int TOTAL_CHUNKS = 100;
	private final Log log = LogFactory.getLog(this.getClass());
	private final List<byte[]> chunks = new ArrayList<byte[]>();
	private FileInputStream fileReader;
	private int fileSize;

	public Chunker(File file) throws IOException {
		initFileReader(file);
		for (int currentChunk = 0; currentChunk < TOTAL_CHUNKS; currentChunk++) {
			chunks.add(readChunk(currentChunk));
		}
		closeFileReader();
	}

	public List<byte[]> getChunks() {
		return new ArrayList<byte[]>(chunks);
	}

	private void closeFileReader() {
		if (fileReader != null) {
			try {
				fileReader.close();
			} catch (IOException e) {
				log.error("Could not close file reader.", e);
			}
		}
		fileReader = null;
	}

	private void initFileReader(File uploadedFile) throws FileNotFoundException, IOException {
		fileReader = new FileInputStream(uploadedFile);
		fileSize = fileReader.available();
	}

	private byte[] readChunk(int chunkNumber) throws IOException {
		int chunkSize = calculateChunkSize(chunkNumber);
		byte[] chunk = new byte[chunkSize];
		fileReader.read(chunk);
		return chunk;
	}

	private int calculateChunkSize(int chunkNumber) {
		int chunkSize = (fileSize / 100) + (fileSize % 100 == 0 ? 0 : 1);
		if (chunkNumber == 99) {
			chunkSize = fileSize - chunkSize * 99;
		}
		return chunkSize;
	}

}
