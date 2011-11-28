package com.all.rds.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity
public class TopPlaylist {

	@Id
	@Column(name="playlistId")
	private String hashcode;

	private String name;

	private Date modifiedDate;

	private Date creationDate;

	private boolean expired;
	
	@Transient
	private List<CachedTrack> tracks;

	public TopPlaylist(String name) {
		this.name = name;
		this.creationDate = new Date();
		this.modifiedDate = new Date();
		this.expired = false;
		this.hashcode = createHashcode();
	}

	public TopPlaylist() {
	}

	public String getHashcode() {
		return hashcode;
	}

	public String getName() {
		return name;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public boolean isExpired() {
		return expired;
	}
	
	public List<CachedTrack> getTracks() {
		return tracks;
	}
	
	public void setTracks(List<CachedTrack> tracks) {
		this.tracks = tracks;
	}

	private String createHashcode() {
		String createdHashcode = name + creationDate.toString() + new Random().nextLong();
		try {
			MessageDigest md = MessageDigest.getInstance("SHA1");
			md.update(createdHashcode.getBytes());
			createdHashcode = toHex(md.digest());
		} catch (NoSuchAlgorithmException e) {
		}
		return createdHashcode;
	}
	
	private String toHex(byte[] hashCode) {
		if (hashCode != null) {
			StringBuilder builder = new StringBuilder();
			for (byte number : hashCode) {
				int value = number & 0x000000ff;
				builder.append(Integer.toHexString(value / 16));
				builder.append(Integer.toHexString(value % 16));
			}
			return builder.toString();
		}
		return null;
	}

}
