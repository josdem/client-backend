package com.all.rds.controller;

import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;

import com.all.rds.model.TopPlaylist;
import com.all.rds.service.TopHundredService;
import com.all.shared.json.JsonConverter;


@SuppressWarnings("unchecked")
public class TestTopHundredController {

	@InjectMocks 
	private TopHundredController controller = new TopHundredController();
	@Mock
	private TopHundredService service;
	
	private String expectedResponse = JsonConverter.toJson(Collections.emptyList());
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void shouldGetCategories() throws Exception {
		when(service.getCategories()).thenReturn(Collections.EMPTY_LIST);
		
		assertEquals(expectedResponse, controller.listCategories());
	}

	@Test
	public void shouldGetTopPlaylists() throws Exception {
		long categoryId = 1L;
		when(service.getTopPlaylists(categoryId)).thenReturn(Collections.EMPTY_LIST);
		
		assertEquals(expectedResponse, controller.getTopPlaylists(categoryId));
	}

	@Test
	public void shouldGetTracks() throws Exception {
		String topPlaylistId="id";
		when(service.getTracks(topPlaylistId)).thenReturn(Collections.EMPTY_LIST);
		
		assertEquals(expectedResponse, controller.getTopPlaylistTracks(topPlaylistId));
	}

	@Test
	public void shouldGetRandomTopPlaylist() throws Exception {
		TopPlaylist topPlaylist = new TopPlaylist();
		when(service.getRandomTopPlaylist()).thenReturn(topPlaylist);
		
		assertEquals(JsonConverter.toJson(topPlaylist), controller.getRandomTopPlaylist());
	}

}
