package com.all.rds.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.all.shared.mc.TrackStatus;

@Entity
public class TrackUploadStatus implements TrackStatus {

	@Id
	private String trackId;
	private Status trackStatus;
	private int lastChunkNumber;
	private Date uploadedOn;
	private Date downloadOn;
	private Integer totalDownloads;

	public TrackUploadStatus() {
	}

	public TrackUploadStatus(String trackId, Status trackStatus, int lastChunkNumber, Date uploadedOn, Date downloadOn,
			Integer totalDownloads) {
		this.trackId = trackId;
		this.trackStatus = trackStatus;
		this.lastChunkNumber = lastChunkNumber;
		this.uploadedOn = uploadedOn;
		this.downloadOn = downloadOn;
		this.totalDownloads = totalDownloads;
	}

	@Override
	public String getTrackId() {
		return trackId;
	}

	@Override
	public Status getTrackStatus() {
		return trackStatus;
	}

	@Override
	public int getLastChunkNumber() {
		return lastChunkNumber;
	}

	public void setTrackId(String trackId) {
		this.trackId = trackId;
	}

	public void setLastChunkNumber(int lastChunkNumber) {
		this.lastChunkNumber = lastChunkNumber;
	}

	public void setTrackStatus(Status trackStatus) {
		this.trackStatus = trackStatus;
	}

	public void setUploadedOn(Date uploadedOn) {
		this.uploadedOn = uploadedOn;
	}

	public Date getUploadedOn() {
		return uploadedOn;
	}

	public void setDownloadOn(Date downloadOn) {
		this.downloadOn = downloadOn;
	}

	public Date getDownloadOn() {
		return downloadOn;
	}

	public void setTotalDownloads(Integer totalDownloads) {
		this.totalDownloads = totalDownloads;
	}

	public Integer getTotalDownloads() {
		return totalDownloads == null ? 0 : totalDownloads;
	}
}
