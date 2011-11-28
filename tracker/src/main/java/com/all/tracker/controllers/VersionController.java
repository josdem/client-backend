package com.all.tracker.controllers;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.all.tracker.model.Version;
import com.all.tracker.service.NoUpdateException;
import com.all.tracker.service.VersionService;

@Controller
@RequestMapping("/version/**")
public class VersionController {

	private static final Log LOG = LogFactory.getLog(VersionController.class);
	public static final String TEXT_HTML = "text/html";
	public static final String ATTACHMENT_FILENAME = "attachment; filename=";
	public static final String CONTENT_DISPOSITION = "Content-Disposition";
	public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
	public static final String CONTENT_MD5 = "Content-MD5";
	@Autowired
	private VersionService versionService;

	private FileInputStreamFactory fileInputStreamFactory = new FileInputStreamFactory();

	@RequestMapping(method = GET)
	public void getVersion(@RequestParam String artifactId, @RequestParam String clientVersion,
			@RequestParam String os, HttpServletResponse response) {

		try {

			Version version = versionService.getUpdatedVersion(artifactId, clientVersion, os);
			setHeaders(response, version);

			// this can be change to a Spring MVC view if used in more than one place
			// for just this case, this is enough
			sendFile(version.getUpdateFile(), response.getOutputStream());

		} catch (NoUpdateException e) {
			notFound(response);
		} catch (Exception e) {
			notFound(response);
			LOG.error("Unexpected error while retrueving update file", e);
		}

	}

	private void notFound(HttpServletResponse response) {
		response.setContentType(TEXT_HTML);
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	}

	private void setHeaders(HttpServletResponse response, Version version) throws IOException {
		File updateFile = version.getUpdateFile();
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType(APPLICATION_OCTET_STREAM);
		response.setContentLength((int) updateFile.length());
		response.setHeader(CONTENT_DISPOSITION, ATTACHMENT_FILENAME + updateFile.getName());
		response.setHeader(CONTENT_MD5, version.getMd5Checksum());
	}

	private void sendFile(File updateFile, ServletOutputStream outputStream) throws Exception {
		FileInputStream fis = null;
		try {

			byte[] readBytes = new byte[1024 * 4];
			int amountBytesRead;
			fis = fileInputStreamFactory.create(updateFile);

			while ((amountBytesRead = fis.read(readBytes)) != -1) {
				outputStream.write(readBytes, 0, amountBytesRead);
			}

		} catch (Exception e) {
			throw e;
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					// ignore
				}
			}
			try {
				outputStream.close();
			} catch (IOException e) {
				// ignore
			}
			fis = null;
			outputStream = null;
		}
	}

	class FileInputStreamFactory {
		FileInputStream create(File updateFile) throws FileNotFoundException {
			return new FileInputStream(updateFile);
		}
	}

}
