package com.all.ultrapeer.services;

import static com.all.networking.NetworkingConstants.NETWORKING_SESSION_ID;
import static com.all.shared.messages.MessEngineConstants.PUSH_REST_UPLOAD_TRACK_REQUEST_TYPE;
import static com.all.shared.messages.MessEngineConstants.REDIRECT_SEEDER_TRACK_LIST_REQUEST_TYPE;
import static com.all.shared.messages.MessEngineConstants.REST_UPDATE_SEEDER_TRACKS;
import static com.all.shared.messages.MessEngineConstants.REST_UPLOAD_TRACK_REQUEST_TYPE;
import static com.all.shared.messages.MessEngineConstants.SEEDER_TRACK_LIST_REQUEST_TYPE;
import static com.all.shared.messages.MessEngineConstants.SEEDER_TRACK_LIST_RESPONSE_TYPE;
import static com.all.shared.messages.MessEngineConstants.TRACK_SEEDERS_REQUEST_TYPE;
import static com.all.shared.messages.MessEngineConstants.TRACK_SEEDERS_RESPONSE_TYPE;
import static com.all.ultrapeer.messages.UltrapeerMessages.Types.USER_PRESENCE_EXPIRED_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import com.all.messengine.Message;
import com.all.messengine.impl.StubMessEngine;
import com.all.peer.commons.messages.ForwardMessage;
import com.all.shared.download.RestUploadRequest;
import com.all.shared.download.SeederTracks;
import com.all.shared.download.TrackSeeders;
import com.all.shared.download.TurnSeederInfo;
import com.all.shared.model.AllMessage;
import com.all.ultrapeer.util.SeedersCache;

public class TestDownloadService {

	@InjectMocks
	private DownloadService service = new DownloadService();
	@Spy
	private StubMessEngine messEngine = new StubMessEngine();
	@Mock
	private SeedersCache seedersCache;

	@Before
	public void initialize() {
		initMocks(this);
		messEngine.setup(service);
	}

	@Test
	public void shouldProccessUpdateSeederInfoRequest() throws Exception {
		TurnSeederInfo seederInfo = new TurnSeederInfo();
		seederInfo.setSeederId("seeder@all.com");
		seederInfo.setTracks(Arrays.asList("hashcode1", "hashcode2"));

		messEngine.send(new AllMessage<TurnSeederInfo>(REST_UPDATE_SEEDER_TRACKS, seederInfo));

		verify(seedersCache).addSeeder(seederInfo.getSeederId(), seederInfo.getTracks());
	}

	@Test
	public void shouldExpireSeederWhenPresenceExpired() throws Exception {
		String seederId = "seeder@all.com";

		messEngine.send(new AllMessage<String>(USER_PRESENCE_EXPIRED_TYPE, seederId));

		verify(seedersCache).removeSeeder(seederId);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldProccessTrackSeedersRequest() throws Exception {
		TrackSeeders trackSeeders = new TrackSeeders("trackId", "leecher@all.com");
		List<String> seeders = Arrays.asList("seeder1@all.com", "seeder2@all.com");
		when(seedersCache.getSeeders(trackSeeders.getTrackId())).thenReturn(seeders);
		AllMessage<TrackSeeders> request = new AllMessage<TrackSeeders>(TRACK_SEEDERS_REQUEST_TYPE, trackSeeders);
		request.putProperty(NETWORKING_SESSION_ID, "sessionId");

		messEngine.send(request);

		AllMessage<TrackSeeders> response = (AllMessage<TrackSeeders>) messEngine.getMessage(TRACK_SEEDERS_RESPONSE_TYPE);
		assertNotNull(response);
		assertEquals(request.getProperty(NETWORKING_SESSION_ID), response.getProperty(NETWORKING_SESSION_ID));
		TrackSeeders actualTrackSeeders = response.getBody();
		assertEquals(seeders, actualTrackSeeders.getSeeders());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldForwardUploadRequestToEachSeeder() throws Exception {
		RestUploadRequest uploadRequest = new RestUploadRequest("leecher@all.com", "trackId");
		AllMessage<RestUploadRequest> requestMessage = new AllMessage<RestUploadRequest>(REST_UPLOAD_TRACK_REQUEST_TYPE,
				uploadRequest);
		requestMessage.putProperty(NETWORKING_SESSION_ID, "sessionId");
		List<String> seeders = Arrays.asList("seeder1@all.com", "seeder2@all.com");
		when(seedersCache.getSeeders(uploadRequest.getTrackId())).thenReturn(seeders);

		messEngine.send(requestMessage);

		List<Message<?>> sentMessages = messEngine.getSentMessages();
		sentMessages.remove(0);
		int totalSeeders = 2;
		assertEquals(totalSeeders, sentMessages.size());
		for (int i = 0; i < totalSeeders; i++) {
			ForwardMessage fwdMessage = (ForwardMessage) sentMessages.get(i);
			assertEquals(seeders.get(i), fwdMessage.getContactId());
			AllMessage<?> body = fwdMessage.getBody();
			assertEquals(PUSH_REST_UPLOAD_TRACK_REQUEST_TYPE, body.getType());
			AllMessage<RestUploadRequest> originalMessage = (AllMessage<RestUploadRequest>) body.getBody();
			assertEquals(requestMessage, originalMessage);
			assertEquals(uploadRequest, originalMessage.getBody());
		}
	}

	@Test
	public void shouldRedirectSeederTracksRequest() throws Exception {
		SeederTracks seederTracks = new SeederTracks("seeder@all.com", "leecher@all.com");
		messEngine.send(new AllMessage<SeederTracks>(SEEDER_TRACK_LIST_REQUEST_TYPE, seederTracks));

		ForwardMessage redirectedMessage = (ForwardMessage) messEngine.getMessage(ForwardMessage.TYPE);
		assertEquals(seederTracks.getSeederId(), redirectedMessage.getContactId());
		AllMessage<?> actualMessage = redirectedMessage.getBody();
		assertEquals(REDIRECT_SEEDER_TRACK_LIST_REQUEST_TYPE, actualMessage.getType());
		assertEquals(seederTracks, actualMessage.getBody());
	}

	@Test
	public void shouldProcessRedirectedSeederTracksRequest() throws Exception {
		SeederTracks seederTracks = new SeederTracks("seeder@all.com", "leecher@all.com");
		List<String> hashcodes = Arrays.asList("hashcode1", "hashcode2");
		when(seedersCache.getHashcodes(seederTracks.getSeederId())).thenReturn(hashcodes);
		messEngine.send(new AllMessage<SeederTracks>(REDIRECT_SEEDER_TRACK_LIST_REQUEST_TYPE, seederTracks));

		ForwardMessage forwardedResponse = (ForwardMessage) messEngine.getMessage(ForwardMessage.TYPE);
		assertEquals(seederTracks.getRequester(), forwardedResponse.getContactId());
		AllMessage<?> actualResponse = forwardedResponse.getBody();
		assertEquals(SEEDER_TRACK_LIST_RESPONSE_TYPE, actualResponse.getType());
		SeederTracks actualSeederTracks = (SeederTracks) actualResponse.getBody();
		assertEquals(seederTracks, actualSeederTracks);
		assertEquals(hashcodes, actualSeederTracks.getTracks());
	}

}
