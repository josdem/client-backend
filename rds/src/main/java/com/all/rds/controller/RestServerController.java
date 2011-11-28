package com.all.rds.controller;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.all.rds.model.CachedTrack;
import com.all.rds.model.TrackUploadStatus;
import com.all.rds.service.TrackService;
import com.all.shared.json.JsonConverter;
import com.all.shared.mc.TrackStatus;
import com.all.shared.model.Track;

@Controller
public class RestServerController {

	private Log log = LogFactory.getLog(this.getClass());
	
	@Autowired
	private TrackService service;

	@RequestMapping(method = POST, value = "/{trackId}/{chunkId}")
	@ResponseStatus(HttpStatus.OK)
	public void uploadTrackChunk(@PathVariable String trackId, @PathVariable int chunkId, @RequestBody byte[] chunkData) {
		if(chunkId==0){
			log.info("\nACTION:UploadStarted:"+trackId);
		}
		service.saveChunk(trackId, chunkId, chunkData);
		if(chunkId==TrackStatus.COMPLETE_UPLOAD){
			log.info("\nACTION:UploadCompleted:"+trackId);
		}
	}

	@RequestMapping(method = GET, value = "/{trackId}/status")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public String getTrackStatus(@PathVariable String trackId) {
		log.info("\nACTION:TrackStatusRequest");
		return JsonConverter.toJson(((TrackUploadStatus) service.getTrackStatus(trackId)));
	}

	@RequestMapping(method = DELETE, value = "/{trackId}/upload")
	@ResponseStatus(HttpStatus.OK)
	public void cancelUpload(@PathVariable String trackId) {
		log.info("\nACTION:UploadCanceled:"+trackId);
		service.cancelUpload(trackId);
	}

	@RequestMapping(method = PUT, value = "/upload/rate")
	@ResponseStatus(HttpStatus.OK)
	public void getUploadRateUrl() {
		log.info("\nACTION:SpeedTest");
		// DO NOTHING: THIS IS ONLY USED BY THE CLIENT TO CALCULATE THE UPLOAD RATE
	}

	@RequestMapping(method = GET, value = "/{trackId}/{chunkId}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public byte[] getTrackChunk(@PathVariable String trackId, @PathVariable int chunkId) {
		byte[] chunk = service.getChunk(trackId, chunkId);
		if(chunk != null && chunk.length > 0) {
			if(chunkId==0){
				log.info("\nACTION:DownloadStarted:"+trackId);
			}else if(chunkId == TrackStatus.COMPLETE_UPLOAD){
				log.info("\nACTION:DownloadCompleted:"+trackId);				
			}
		}
		return chunk;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(method = POST, value = "/availability")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public String filterTracksByAvailability(@RequestBody String jsonIds) {
		log.info("\nACTION:RequestTrackAvailability");
		return JsonConverter.toJson(service
				.filterTracksByAvailability(JsonConverter.toCollection(jsonIds, ArrayList.class)));
	}

	@RequestMapping(method = PUT, value = "/{trackId}/metadata")
	@ResponseStatus(HttpStatus.OK)
	public void uploadTrackMetadata(@PathVariable String trackId, @RequestBody String jsonTrack) {
		log.info("\nACTION:UploadMetadata:"+trackId);
		Track track = JsonConverter.toBean(jsonTrack, CachedTrack.class);
		if (!trackId.equals(track.getHashcode())) {
			throw new IllegalArgumentException("Invalid metadata for track " + trackId);
		}
		service.storeMetadata(track);
	}

	@RequestMapping(method = POST, value = "/search")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public String searchTracks(@RequestBody String keyword) {
		log.info("\nACTION:SearchTracks:'"+keyword+"'");
		return JsonConverter.toJson(service.findTracksByKeyword(keyword));
	}


	@RequestMapping(method = GET, value = "/health")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public String getServerStatus() {
		log.info("\nACTION:HealthCheck");
		return HttpStatus.OK.name();
	}

}
