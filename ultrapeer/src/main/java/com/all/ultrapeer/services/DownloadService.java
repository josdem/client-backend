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
import static com.all.ultrapeer.messages.UltrapeerMessages.Properties.MP_UNWRAP_BEFORE_SEND;
import static com.all.ultrapeer.messages.UltrapeerMessages.Types.USER_PRESENCE_EXPIRED_TYPE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.all.messengine.MessEngine;
import com.all.messengine.MessageMethod;
import com.all.peer.commons.messages.ForwardMessage;
import com.all.shared.download.RestUploadRequest;
import com.all.shared.download.SeederTracks;
import com.all.shared.download.TrackSeeders;
import com.all.shared.download.TurnSeederInfo;
import com.all.shared.model.AllMessage;
import com.all.ultrapeer.util.SeedersCache;

@Service
public class DownloadService {

	@Autowired
	private MessEngine messEngine;
	@Autowired
	private SeedersCache seedersCache;

	@MessageMethod(REST_UPDATE_SEEDER_TRACKS)
	public void updateSeederInfo(TurnSeederInfo seederInfo) {
		seedersCache.addSeeder(seederInfo.getSeederId(), seederInfo.getTracks());
	}

	@MessageMethod(USER_PRESENCE_EXPIRED_TYPE)
	public void expireSeeder(String seederId) {
		seedersCache.removeSeeder(seederId);
	}

	@MessageMethod(TRACK_SEEDERS_REQUEST_TYPE)
	public void processTrackSeedersRequest(AllMessage<TrackSeeders> request) {
		TrackSeeders trackSeeders = request.getBody();
		trackSeeders.setSeeders(seedersCache.getSeeders(trackSeeders.getTrackId()));
		AllMessage<TrackSeeders> response = new AllMessage<TrackSeeders>(TRACK_SEEDERS_RESPONSE_TYPE, trackSeeders);
		response.putProperty(NETWORKING_SESSION_ID, request.getProperty(NETWORKING_SESSION_ID));
		messEngine.send(response);
	}

	@MessageMethod(REST_UPLOAD_TRACK_REQUEST_TYPE)
	public void forwardUploadRequestToSeeders(AllMessage<RestUploadRequest> request) {
		RestUploadRequest uploadRequest = request.getBody();
		for (String seeder : seedersCache.getSeeders(uploadRequest.getTrackId())) {
			AllMessage<AllMessage<RestUploadRequest>> pushRequest = new AllMessage<AllMessage<RestUploadRequest>>(
					PUSH_REST_UPLOAD_TRACK_REQUEST_TYPE, request);
			pushRequest.putProperty(MP_UNWRAP_BEFORE_SEND, Boolean.toString(true));
			messEngine.send(new ForwardMessage(pushRequest, seeder));
		}
	}

	@MessageMethod(SEEDER_TRACK_LIST_REQUEST_TYPE)
	public void processSeederTracksRequest(SeederTracks request) {
		AllMessage<SeederTracks> redirectRequest = new AllMessage<SeederTracks>(REDIRECT_SEEDER_TRACK_LIST_REQUEST_TYPE,
				request);
		messEngine.send(new ForwardMessage(redirectRequest, request.getSeederId()));
	}

	@MessageMethod(REDIRECT_SEEDER_TRACK_LIST_REQUEST_TYPE)
	public void processRedirectedSeederTracksRequest(SeederTracks request) {
		request.setTracks(seedersCache.getHashcodes(request.getSeederId()));
		AllMessage<SeederTracks> response = new AllMessage<SeederTracks>(SEEDER_TRACK_LIST_RESPONSE_TYPE, request);
		messEngine.send(new ForwardMessage(response, request.getRequester()));
	}

}
