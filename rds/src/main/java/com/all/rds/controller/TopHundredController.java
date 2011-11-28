package com.all.rds.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.all.rds.service.TopHundredService;
import com.all.shared.json.JsonConverter;

@Controller
public class TopHundredController {

	private Log log = LogFactory.getLog(this.getClass());

	@Autowired
	private TopHundredService topHundredService;
	
	@RequestMapping(method = GET, value = "/top/categories")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public String listCategories() {
		log.info("\nACTION:ListCategories:");
		return JsonConverter.toJson(topHundredService.getCategories());
	}

	@RequestMapping(method = GET, value = "/top/{categoryId}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public String getTopPlaylists(@PathVariable Long categoryId) {
		log.info("\nACTION:ListTopPlaylistByCategory:" + categoryId);
		return JsonConverter.toJson(topHundredService.getTopPlaylists(categoryId));
	}

	@RequestMapping(method = GET, value = "/top/playlist/{playlistId}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public String getTopPlaylistTracks(@PathVariable String playlistId) {
		log.info("\nACTION:ListTopPlaylistTracks:" + playlistId);
		return JsonConverter.toJson(topHundredService.getTracks(playlistId));
	}
	
	@RequestMapping(method = GET, value = "/top/random")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public String getRandomTopPlaylist() {
		log.info("\nACTION:RandomTopPlaylist:");
		return JsonConverter.toJson(topHundredService.getRandomTopPlaylist());
	}


}
