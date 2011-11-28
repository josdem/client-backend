package com.all.rds.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.all.shared.mc.TrackStatus.Status;

public class TestTrackUploadStatus {

	@Test
	public void shoudlcreateTrackStats() throws Exception {
		String trackId = "1234567890";
		TrackUploadStatus track = new TrackUploadStatus();
		track.setTrackId(trackId);
		track.setLastChunkNumber(67);
		track.setTrackStatus(Status.UPLOADED);
		assertEquals(trackId, track.getTrackId());
		assertEquals(67, track.getLastChunkNumber());
		assertEquals(Status.UPLOADED, track.getTrackStatus());
	}

}
