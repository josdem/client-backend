package com.all.tracker.controllers;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.all.tracker.UnitTestCase;
import com.all.tracker.controllers.VersionController.FileInputStreamFactory;
import com.all.tracker.model.Version;
import com.all.tracker.service.NoUpdateException;
import com.all.tracker.service.VersionService;

public class TestVersionController extends UnitTestCase {
	
	@InjectMocks
	private VersionController versionController = new VersionController();
	@Mock
	private VersionService versionService;
	@Mock
	private FileInputStreamFactory fileInputStreamFactory;
	@Mock
	private HttpServletResponse response;
	@Mock
	private File updateFile;
	@Mock
	private ServletOutputStream outputStream;
	@Mock
	private FileInputStream fis;
	@Mock
	private Version version; 
	
	private String artifactId = "artifactId";

	private String clientVersion = "clientVersion";

	private String os = "os";

	private String md5Checksum = "MD5Checksum";

	@Before
	public void setup() throws Exception {
		when(version.getArtifactId()).thenReturn(artifactId);
		when(version.getVersion()).thenReturn(clientVersion);
		when(version.getUpdateFile()).thenReturn(updateFile);
		when(version.getMd5Checksum()).thenReturn(md5Checksum);
	}
	
	@Test
	public void shouldGetArtifactVersion() throws Exception {
		int fileLength = 10;

		when(versionService.getUpdatedVersion(artifactId, clientVersion, os)).thenReturn(version);
		when(updateFile.length()).thenReturn((long)fileLength);
		when(response.getOutputStream()).thenReturn(outputStream);
		when(fileInputStreamFactory.create(updateFile)).thenReturn(fis);
		when(fis.read(any(byte[].class))).thenReturn(fileLength, -1);
		
		versionController.getVersion(artifactId, clientVersion, os, response);
		
		verify(response).setStatus(HttpServletResponse.SC_OK);
		verify(response).setContentType(VersionController.APPLICATION_OCTET_STREAM);
		verify(response).setContentLength(fileLength);
		verify(response).setHeader(VersionController.CONTENT_DISPOSITION, VersionController.ATTACHMENT_FILENAME + updateFile.getName());
		verify(response).setHeader(VersionController.CONTENT_MD5, md5Checksum);
		
		verify(outputStream).write(any(byte[].class), eq(0), eq(fileLength));
		
		verify(fis).close();
	}
	
	@Test
	public void shouldGetNotFoundIfNoNewUpdates() throws Exception {
		when(versionService.getUpdatedVersion(artifactId, clientVersion, os)).thenThrow(new NoUpdateException(""));
		
		versionController.getVersion(artifactId, clientVersion, os, response);
		
		verify(response).setContentType(VersionController.TEXT_HTML);
		verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
		
		verify(response, never()).getOutputStream();
	}
	
	@Test
	public void shouldGetNotFoundIfFailedreadingFile() throws Exception {
		int fileLength = 10;

		when(versionService.getUpdatedVersion(artifactId, clientVersion, os)).thenReturn(version);
		when(updateFile.length()).thenReturn((long)fileLength);
		when(response.getOutputStream()).thenReturn(outputStream);
		when(fileInputStreamFactory.create(updateFile)).thenReturn(fis);
		when(fis.read(any(byte[].class))).thenThrow(new IOException());
		doThrow(new IOException()).when(fis).close();
		
		versionController.getVersion(artifactId, clientVersion, os, response);
		
		verify(fis).close();
		verify(response).setContentType(VersionController.TEXT_HTML);
		verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
	}
	
}
