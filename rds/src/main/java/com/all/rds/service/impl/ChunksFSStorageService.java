package com.all.rds.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;

import com.all.rds.service.ChunkStorageService;

public class ChunksFSStorageService implements ChunkStorageService {
	private static final byte[] EMPTY_CHUNK = new byte[] {};

	private final Log log = LogFactory.getLog(this.getClass());

	@Value("#{settings.chunks.path}")
	private String homeDirForChunks;

	@Override
	public boolean put(String trackId, int chunkId, byte[] chunkData) {
		createChunkDirectory(trackId);
		File chunkFile = getChunkFile(trackId, chunkId);
		if (!chunkFile.exists()) {
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(chunkFile, false);
				fos.write(chunkData);
				log.debug("Writing chunk : " + chunkId + " track :" + trackId);
				return true;
			} catch (IOException e) {
				log.error("Could not save file chunk to disk.", e);
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						log.error("Could not close FileOutputStream.", e);
					}
				}
			}
		}
		return false;
	}

	@Override
	public byte[] get(String trackId, int chunkNumber) {
		try {
			byte[] chunk = readChunk(trackId, chunkNumber);
			log.debug("reading chunk : " + chunkNumber + " trackId : " + trackId + " bytes read : " + chunk.length);
			return chunk == null ? EMPTY_CHUNK : chunk;
		} catch (FileNotFoundException e) {
			log.error("Could not find file.", e);
		} catch (IOException e) {
			log.error("IO problem reading this track chunk: " + trackId + " chunk : " + chunkNumber, e);
		}
		return EMPTY_CHUNK;
	}

	private byte[] readChunk(String trackId, int chunkNumber) throws IOException {
		File chunkFile = getChunkFile(trackId, chunkNumber);
		FileInputStream fis = new FileInputStream(chunkFile);
		byte[] chunk = new byte[fis.available()];
		fis.read(chunk);
		fis.close();
		return chunk;
	}

	private void createChunkDirectory(String trackId) {
		String dirPath = getTrackDirectoryPath(trackId);
		File dir = new File(dirPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	private File getChunkFile(String trackId, int chunkNumber) {
		String chunkPath = getTrackDirectoryPath(trackId) + chunkNumber;
		return new File(chunkPath);
	}

	private String getTrackDirectoryPath(String trackId) {
		StringBuilder sb = new StringBuilder();
		sb.append(getHomeDirForChunks());
		for (int i = 0; i < trackId.length(); i++) {
			sb.append(trackId.charAt(i));
			if ((i + 1) % 2 == 0 && i > 0) {
				sb.append("/");
			}
		}
		return sb.toString();
	}

	public String getHomeDirForChunks() {
		return homeDirForChunks;
	}

}
