package com.all.tracker.model;

import java.io.File;
import java.io.Serializable;
import java.util.regex.Pattern;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity
public class Version implements Serializable, Comparable<Version> {

	private static final long serialVersionUID = 1L;
	private static final int MAX_WIDTH = 7;
	private static final String DOT = ".";
	private static final String NORMALIZER_FORMAT = "%" + MAX_WIDTH + "s";

	@Id
	private String artifactId;
	private String version;
	@Transient
	private File updateFile;
	@Transient
	private String md5Checksum;

	@Deprecated
	public Version() {
	}

	public Version(String artifactId, String version) {
		this.artifactId = artifactId;
		this.version = version;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getVersion() {
		return version;
	}

	public void setUpdateFile(File updateFile) {
		this.updateFile = updateFile;
	}

	public File getUpdateFile() {
		return updateFile;
	}

	public void setMd5Checksum(String md5Checksum) {
		this.md5Checksum = md5Checksum;
	}
	
	public String getMd5Checksum() {
		return md5Checksum;
	}

	@Override
	public int compareTo(Version other) {
		if(other == null) {
			throw new IllegalArgumentException("Parameter received is null");
		}
		
		return this.getNormalizedVersion().compareToIgnoreCase(other.getNormalizedVersion());
	}

	private String getNormalizedVersion() {
		String[] versionParts = Pattern.compile(DOT, Pattern.LITERAL).split(version);
		StringBuilder sb = new StringBuilder();
		for (String versionPart : versionParts) {
			sb.append(String.format(NORMALIZER_FORMAT, versionPart));
		}
		return sb.toString();
	}

}
