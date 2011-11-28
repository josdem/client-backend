package com.all.rds.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONArray;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;

import com.all.rds.model.CachedTrack;
import com.all.rds.model.TrackUploadStatus;
import com.all.rds.service.TrackService;
import com.all.shared.json.JsonConverter;
import com.all.shared.mc.TrackSearchResult;
import com.all.shared.mc.TrackStatus.Status;
import com.all.shared.model.RemoteTrack;
import com.all.testing.MockInyectRunner;
import com.all.testing.UnderTest;

@RunWith(MockInyectRunner.class)
public class TestRestServerController {

	@UnderTest
	private RestServerController controller;
	@Mock
	private TrackService service;
	private String trackId = "1234567890";
	private int chunkId = 76;
	private byte[] chunkData = "testing rest server ".getBytes();
	private TrackUploadStatus trackUploadStatus = new TrackUploadStatus(trackId, Status.UPLOADING, 1, new Date(),
			new Date(), 10);

	@Test
	public void shuldUploadTrackChunk() {
		controller.uploadTrackChunk(trackId, chunkId, chunkData);
		verify(service).saveChunk(trackId, chunkId, chunkData);
	}

	@Test
	public void shouldGetTrackStatus() {
		when(service.getTrackStatus(trackId)).thenReturn(trackUploadStatus);
		controller.getTrackStatus(trackId);
		verify(service).getTrackStatus(trackId);
	}

	@Test
	public void shouldRemoveTrackStatsFromTrackStatus() {
		when(service.getTrackStatus(trackId)).thenReturn(trackUploadStatus);
		controller.getTrackStatus(trackId);
		verify(service).getTrackStatus(trackId);
	}

	@Test
	public void shouldCancelUpload() {
		controller.cancelUpload(trackId);
		verify(service).cancelUpload(trackId);
	}

	@Test
	public void shouldGetUploadRateUrl() {
		controller.getUploadRateUrl();
	}

	@Test
	public void shouldGetTrackChunk() {
		controller.getTrackChunk(trackId, chunkId);
		verify(service).getChunk(trackId, chunkId);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldGetAvailableTracks() throws Exception {
		List<String> expectedIds = Arrays.asList(new String[] { "trackA", "trackB", "trackC" });
		String jsonIds = JSONArray.fromObject(expectedIds).toString();
		when(service.filterTracksByAvailability(expectedIds)).thenReturn(expectedIds);
		String resultIds = controller.filterTracksByAvailability(jsonIds);
		JSONArray jsonArray = JSONArray.fromObject(resultIds);
		List<String> actualIds = new ArrayList<String>(JSONArray.toCollection(jsonArray));
		assertEquals(expectedIds, actualIds);
	}

	@Test
	public void shouldStoreMetadata() throws Exception {
		RemoteTrack track = new RemoteTrack();
		track.setHashcode(trackId);

		controller.uploadTrackMetadata(trackId, JsonConverter.toJson(track));

		verify(service).storeMetadata(isA(CachedTrack.class));

	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldNotStoreMetadataIfTrackIdDoesNotMatch() throws Exception {
		RemoteTrack track = new RemoteTrack();
		track.setHashcode(trackId);
		controller.uploadTrackMetadata("otherId", JsonConverter.toJson(track));
	}
	
	@Test
	public void shouldSearchTracksByKeyword() throws Exception {
		String keyword = "some keyword";
		when(service.findTracksByKeyword(keyword)).thenReturn(new ArrayList<TrackSearchResult>());

		String result = controller.searchTracks(keyword);
		
		verify(service).findTracksByKeyword(keyword);
		assertEquals(JsonConverter.toJson(Collections.emptyList()), result);
	}
	
	@Test
	public void shouldRespondHealthStatus() throws Exception {
		assertEquals(HttpStatus.OK.name(), controller.getServerStatus());
	}

}
