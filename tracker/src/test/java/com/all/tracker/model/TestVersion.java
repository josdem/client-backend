package com.all.tracker.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestVersion {

	private String artifactId = "artifactId"; 
	
	@Test
	public void shouldCompareBasicVersions() throws Exception {
		Version versionOne = new Version(artifactId, "1.0.0");
		Version versionTwo = new Version(artifactId, "2.0.0");
		
		assertTrue(versionOne.compareTo(versionTwo) < 0);
		assertTrue(versionOne.compareTo(versionOne) == 0);
		assertTrue(versionTwo.compareTo(versionOne) > 0);

		versionOne = new Version(artifactId, "0.1.0");
		versionTwo = new Version(artifactId, "0.2.0");
		
		assertTrue(versionOne.compareTo(versionTwo) < 0);
		assertTrue(versionOne.compareTo(versionOne) == 0);
		assertTrue(versionTwo.compareTo(versionOne) > 0);
	}
	
	@Test
	public void shouldCompareAllLikeVersions() throws Exception {
		Version newerVersion = new Version(artifactId, "0.0.17");
		Version olderVersion = new Version(artifactId, "0.0.16");
		
		assertTrue(newerVersion.compareTo(olderVersion) > 0);
		
		newerVersion = new Version(artifactId, "0.0.17.1");
		olderVersion = new Version(artifactId, "0.0.17");
		
		assertTrue(newerVersion.compareTo(olderVersion) > 0);
		
		newerVersion = new Version(artifactId, "0.0.17");
		olderVersion = new Version(artifactId, "0.0.16.1");
		
		assertTrue(newerVersion.compareTo(olderVersion) > 0);
		
		newerVersion = new Version(artifactId, "0.0.17.");
		olderVersion = new Version(artifactId, "0.0.17");
		
		assertTrue(newerVersion.compareTo(olderVersion) == 0);
		
		newerVersion = new Version(artifactId, "0.0.17b"); //beta?
		olderVersion = new Version(artifactId, "0.0.17a"); //alfa?
		
		assertTrue(newerVersion.compareTo(olderVersion) > 0);
		
	}
	
}
