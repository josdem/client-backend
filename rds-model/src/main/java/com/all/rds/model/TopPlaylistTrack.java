package com.all.rds.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Index;

@Entity
@SuppressWarnings("unused")
public class TopPlaylistTrack {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Index(name = "PLAYLIST_IDX")
	private String playlist;
	private String track;
	private Integer numTrack;

	public TopPlaylistTrack() {
	}

	public TopPlaylistTrack(String playlist, String track, Integer numTrack) {
		this.playlist = playlist;
		this.track = track;
		this.numTrack = numTrack;
	}

}
