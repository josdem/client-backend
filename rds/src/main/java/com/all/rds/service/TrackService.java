package com.all.rds.service;

import java.util.List;

import com.all.shared.mc.TrackSearchResult;
import com.all.shared.mc.TrackStatus;
import com.all.shared.model.Track;

public interface TrackService {

	public void saveChunk(String trackId, int chunkId, byte[] chunkData);

	public byte[] getChunk(String trackId, int chunkNumber);

	public TrackStatus getTrackStatus(String trackId);

	public void cancelUpload(String trackId);

	public List<String> filterTracksByAvailability(List<String> trackIds);

	public void storeMetadata(Track track);

	public List<TrackSearchResult> findTracksByKeyword(String keyword);
}
