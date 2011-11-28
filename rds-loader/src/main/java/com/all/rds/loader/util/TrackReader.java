package com.all.rds.loader.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.all.rds.model.CachedTrack;
import com.all.rds.model.CachedTrackChunk;
import com.all.rds.model.TrackUploadStatus;
import com.all.shared.mc.TrackStatus;

public class TrackReader {

	private static final MetadataReader METADATA_READER = new JAudioTaggerMetadataReader();
	private final List<CachedTrackChunk> chunks;
	private final String hashcode;
	private final CachedTrack track;
	private final TrackUploadStatus status;

	public TrackReader(File file) throws IOException, IllegalArgumentException {
		this.hashcode = Hashcoder.createHashCode(file);
		this.chunks = readChunks(file);
		this.track = readTrack(file);
		this.status = createCompleteStatus();
	}

	private CachedTrack readTrack(File file) {
		Metadata metadata = METADATA_READER.read(file);
		if (metadata == null) {
			throw new IllegalArgumentException("Could not read metadata for file " + file);
		}
		CachedTrack track = new CachedTrack();
		track.setHashcode(hashcode);
		track.setName(getFixName(metadata.getName()));
		track.setArtist(metadata.getArtist());
		track.setAlbum(metadata.getAlbum());
		track.setBitRate(metadata.getBitRate());
		track.setDuration(metadata.getDuration());
		String filePath = file.getAbsolutePath();
		String format = filePath.substring(filePath.lastIndexOf("."));
		track.setFileFormat(format.replace(".", ""));
		track.setFileName(getFixName(file.getName()));
		track.setGenre(metadata.getGenre());
		track.setSampleRate(metadata.getSampleRate());
		track.setSize(file.length());
		track.setYear(metadata.getYear());
		return track;
	}
	
	private String getFixName(String name) {
		return name.length() < 255 ? name : name.substring(1, 254);
	}

	private List<CachedTrackChunk> readChunks(File file) throws IOException {
		Chunker chunker = new Chunker(file);
		List<byte[]> chunks = chunker.getChunks();
		List<CachedTrackChunk> trackChunks = new ArrayList<CachedTrackChunk>();
		for (int i = 0; i < chunks.size(); i++) {
			trackChunks.add(new CachedTrackChunk(hashcode, i, chunks.get(i)));
		}
		return trackChunks;
	}


	private TrackUploadStatus createCompleteStatus() {
		TrackUploadStatus status = new TrackUploadStatus();
		status.setTrackId(hashcode);
		status.setTrackStatus(TrackStatus.Status.UPLOADED);
		status.setLastChunkNumber(TrackStatus.COMPLETE_UPLOAD);
		status.setUploadedOn(new Date());
		return status;
	}

	public String getHashcode() {
		return hashcode;
	}
	
	public List<CachedTrackChunk> getChunks() {
		return chunks;
	}
	
	public CachedTrack getTrack() {
		return track;
	}
	
	public TrackUploadStatus getStatus() {
		return status;
	}

	public Collection<Object> getTrackEntities(){
		Collection<Object> trackEntities = new ArrayList<Object>();
		trackEntities.add(track);
		trackEntities.addAll(chunks);
		trackEntities.add(status);
		return trackEntities;
	}
	
}
