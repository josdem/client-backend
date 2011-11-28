package com.all.rds.service;

import java.util.List;

import com.all.rds.model.CachedTrack;
import com.all.rds.model.Category;
import com.all.rds.model.TopPlaylist;

public interface TopHundredService {

	List<Category> getCategories();
	
	List<TopPlaylist> getTopPlaylists(Long categoryId);
	
	List<CachedTrack> getTracks(String topPlaylistId);

	TopPlaylist getRandomTopPlaylist();
	
}
