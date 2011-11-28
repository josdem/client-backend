package com.all.rds.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import com.all.shared.model.Track;

@Entity
@Indexed
public class CachedTrack implements Track {

	private static final long serialVersionUID = 1L;
	@Id
	private String hashcode;
	@Field(index = Index.TOKENIZED, store = Store.NO)
	private String album;
	@Field(index = Index.TOKENIZED, store = Store.NO)
	private String artist;
	private String bitRate;
	private String downloadString;
	private int duration;
	private String fileFormat;
	private String fileName;
	private String genre;
	@Field(index = Index.TOKENIZED, store = Store.NO)
	private String name;
	private String sampleRate;
	private long size;
	private String year;
	private boolean vbr;
	@Transient
	private final Set<String> extraKeywords = new HashSet<String>();

	@Override
	public String getAlbum() {
		return album;
	}

	@Override
	public String getArtist() {
		return artist;
	}

	@Override
	public String getBitRate() {
		return bitRate;
	}

	@Override
	public Date getDateAdded() {
		return null;
	}

	@Override
	public String getDownloadString() {
		return downloadString;
	}

	@Override
	public int getDuration() {
		return duration;
	}

	@Override
	public String getFileFormat() {
		return fileFormat;
	}

	@Override
	public String getFileName() {
		return fileName;
	}

	@Override
	public String getGenre() {
		return genre;
	}

	@Override
	public String getHashcode() {
		return hashcode;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getSampleRate() {
		return sampleRate;
	}

	@Override
	public long getSize() {
		return size;
	}

	@Override
	public String getYear() {
		return year;
	}

	@Override
	public boolean isVBR() {
		return vbr;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public void setBitRate(String bitRate) {
		this.bitRate = bitRate;
	}

	public void setDownloadString(String downloadString) {
		this.downloadString = downloadString;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public void setFileFormat(String format) {
		this.fileFormat = format;
	}

	public void setFileName(String name) {
		this.fileName = name;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public void setHashcode(String hashcode) {
		this.hashcode = hashcode;
	}

	public void setName(String firstTitle) {
		this.name = firstTitle;
	}

	public void setSampleRate(String sampleRate) {
		this.sampleRate = sampleRate;
	}

	public void setSize(long fileSize) {
		this.size = fileSize;
	}

	public void setVBR(boolean VBR) {
		this.vbr = VBR;
	}

	public void setYear(String firstYear) {
		this.year = firstYear;
	}

	// ///////////////////**EXTRA-STUFF**\\\\\\\\\\\\\\\\\\\\\\\
	@Override
	public String getAlbumArtist() {
		return null;
	}

	@Override
	public String getArtistAlbum() {
		return null;
	}

	@Override
	public String getBitRateDesc() {
		return null;
	}

	@Override
	public String getDurationMinutes() {
		return null;
	}

	@Override
	public String getFormattedSize() {
		return null;
	}

	@Override
	public Date getLastPlayed() {
		return null;
	}

	@Override
	public Date getLastSkipped() {
		return null;
	}

	@Override
	public int getPlaycount() {
		return 0;
	}

	@Override
	public int getRating() {
		return 0;
	}

	@Override
	public int getSkips() {
		return 0;
	}

	@Override
	public String getTrackNumber() {
		return null;
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public boolean isNewContent() {
		return false;
	}

	public void setExtraKeywords(Set<String> extraKeywords) {
		if (extraKeywords != null) {
			this.extraKeywords.addAll(extraKeywords);
		}
	}

}
