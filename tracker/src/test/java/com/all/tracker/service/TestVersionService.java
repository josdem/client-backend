package com.all.tracker.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.all.commons.Md5FileGenerator;
import com.all.tracker.UnitTestCase;
import com.all.tracker.model.Version;

public class TestVersionService extends UnitTestCase {

	@InjectMocks
	private VersionService versionService = new VersionService();
	@Mock
	private Properties settings;
	@Mock
	private HibernateTemplate ht;
	@Spy
	@SuppressWarnings("unused")
	private Md5FileGenerator md5FileGenerator = new Md5FileGenerator();

	private String clientArtifactId = "artifactId";

	private String clientVersion = "0.0.0.1";

	private String os = "os";
	
	private String serverVersion = "0.0.0.2";
	
	private String expectedMd5Checksum = "1B2M2Y8AsgTpgAmY7PhCfg==";
	
	private ArrayList<Version> allVersions = new ArrayList<Version>();

	@Before
	public void setup() {
		when(settings.getProperty(VersionService.UPDATE_FILE_PATH)).thenReturn("src/test/resources");
		versionService.initialize();
	}
	
	@Test
	public void shouldInitialize() throws Exception {
		//check if no exception is thrown
		versionService.initialize();
	}

	@Test(expected = IllegalStateException.class)
	public void shouldFailIfProperyNotFoundDuringInit() throws Exception {
		reset(settings);
		
		versionService.initialize();
	}

	@Test
	public void shouldGetUpdatedVersion() throws Exception {
		allVersions.add(new Version(clientArtifactId, serverVersion));
		when(ht.find(VersionService.QUERY_VERSION, clientArtifactId)).thenReturn(allVersions);
		
		Version version = versionService.getUpdatedVersion(clientArtifactId, clientVersion, os);
		
		assertNotNull(version.getUpdateFile());
		assertEquals(expectedMd5Checksum, version.getMd5Checksum());
		assertEquals(clientArtifactId, version.getArtifactId());
		assertEquals(serverVersion, version.getVersion());
	}

	@Test
	public void shouldGetUpdatedVersionAmongManyVersions() throws Exception {
		allVersions.add(new Version(clientArtifactId, serverVersion));
		allVersions.add(new Version(clientArtifactId, "0.0.0.1"));
		allVersions.add(new Version(clientArtifactId, "0.0.0.1.3"));
		allVersions.add(new Version(clientArtifactId, "0.0.0.0"));
		allVersions.add(new Version(clientArtifactId, "0.0.0.1.1"));
		when(ht.find(VersionService.QUERY_VERSION, clientArtifactId)).thenReturn(allVersions);
		
		Version version = versionService.getUpdatedVersion(clientArtifactId, clientVersion, os);
		
		assertNotNull(version.getUpdateFile());
		assertEquals(expectedMd5Checksum, version.getMd5Checksum());
		assertEquals(clientArtifactId, version.getArtifactId());
		assertEquals(serverVersion, version.getVersion());
	}
	
	@Test(expected=NoUpdateException.class)
	public void shouldFailIfArtifactNotFoundOnGetUpdatedVersion() throws Exception {
		when(ht.find(VersionService.QUERY_VERSION, clientArtifactId)).thenReturn(Collections.EMPTY_LIST);

		versionService.getUpdatedVersion(clientArtifactId, clientVersion, os);
	}

	@Test(expected=NoUpdateException.class)
	public void shouldThrowNoUpdaeFoundIfWeAreUpToDate() throws Exception {
		Version versionEntity = new Version(clientArtifactId, clientVersion);
		allVersions.add(versionEntity);
		when(ht.find(VersionService.QUERY_VERSION, clientArtifactId)).thenReturn(allVersions);
		
		versionService.getUpdatedVersion(clientArtifactId, clientVersion, os);
	}
	
	@Test(expected=NoUpdateException.class)
	public void shouldFailIfFileDoesNotExistOnGetUpdatedVersion() throws Exception {
		String inexistentVersion = "inexistent";
		Version versionEntity = new Version(clientArtifactId, inexistentVersion);
		allVersions.add(versionEntity);
		when(ht.find(VersionService.QUERY_VERSION, clientArtifactId)).thenReturn(allVersions);
		
		versionService.getUpdatedVersion(clientArtifactId, clientVersion, os);
	}
	
	@Test(expected=NoUpdateException.class)
	public void shouldThrowNoUpdateExceptionIfNoUpdateAvailableOnGetUpdatedVersion() throws Exception {
		Version versionEntity = new Version(clientArtifactId, clientVersion);
		when(ht.get(Version.class, clientArtifactId)).thenReturn(versionEntity);
		
		versionService.getUpdatedVersion(clientArtifactId, clientVersion, os);
	}
	
}
