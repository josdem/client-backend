package com.all.tracker.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.all.commons.Md5FileGenerator;
import com.all.tracker.model.Version;

@Service
public class VersionService {

	public static final String QUERY_VERSION = "from Version where artifactId = ?";

	private static final Log log = LogFactory.getLog(VersionService.class);

	private static final String EXTENSION = ".zip";

	private static final String UNDERSCORE = "_";

	public static final String UPDATE_FILE_PATH = "version.update.base.file.path";

	@Autowired
	private HibernateTemplate ht;
	@Autowired
	private Properties settings;

	private Md5FileGenerator md5FileGenerator = new Md5FileGenerator();

	private String updatePath;

	@PostConstruct
	public void initialize() {
		updatePath = settings.getProperty(UPDATE_FILE_PATH);

		if (updatePath == null) {
			throw new IllegalStateException("Property not found in settings.properties file: " + UPDATE_FILE_PATH);
		}

	}

	@SuppressWarnings("unchecked")
	public Version getUpdatedVersion(String clientArtifactId, String clientVersion, String os) throws NoUpdateException, FileNotFoundException {

		List<Version> allVersions = ht.find(QUERY_VERSION, clientArtifactId);

		if (allVersions.isEmpty()) {
			throw new NoUpdateException("Artifact ID not found", new IllegalArgumentException("Could not find the artifactId: " + clientArtifactId));
		}

		Version latestUpdateVersion = findLatestVersion(allVersions, new Version(clientArtifactId, clientVersion));

		if (latestUpdateVersion == null) {
			throw new NoUpdateException("No new updates found for artifact ID: " + clientArtifactId);
		}

		String artifactId = latestUpdateVersion.getArtifactId();
		String latestVersion = latestUpdateVersion.getVersion();

		String updateFilePath = getUpdateFilePath(artifactId, latestVersion, os);

		File updateFile = new File(updateFilePath);
		log.debug("update file path: " + updateFilePath);

		if (!updateFile.exists()) {
			throw new NoUpdateException("Cannot find update file", new FileNotFoundException(updateFilePath));
		}
		
		latestUpdateVersion.setUpdateFile(updateFile);
		latestUpdateVersion.setMd5Checksum(calculateMd5Checksum(updateFile));
		return latestUpdateVersion;
	}

	private Version findLatestVersion(List<Version> versions, Version clientVersion) {
		Version latestVersion = null;

		for (Version version : versions) {
			if (version.compareTo(clientVersion) > 0) {
				latestVersion = version;
				clientVersion = version;
			}
		}

		return latestVersion;
	}

	// TODO cache the md5 checksum so it gets only calculated once
	private String calculateMd5Checksum(File updateFile) {
		byte[] calculatedMd5Checksum = md5FileGenerator.calculateMd5Checksum(updateFile);
		return md5FileGenerator.getByteToBase64String(calculatedMd5Checksum);
	}

	private String getUpdateFilePath(String artifactId, String version, String os) {
		StringBuilder sb = new StringBuilder(updatePath);
		sb.append(File.separator);
		sb.append(artifactId.toUpperCase());
		sb.append(File.separator);
		sb.append(version.toUpperCase());
		sb.append(File.separator);
		sb.append(artifactId.toUpperCase());
		sb.append(UNDERSCORE);
		sb.append(version.toUpperCase());
		sb.append(UNDERSCORE);
		sb.append(os.toUpperCase());
		sb.append(EXTENSION);
		return sb.toString();
	}

}
