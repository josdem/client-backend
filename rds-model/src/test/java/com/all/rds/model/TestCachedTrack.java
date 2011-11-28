package com.all.rds.model;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Test;

import com.all.shared.json.JsonConverter;
import com.all.shared.model.RemoteTrack;
import com.all.shared.model.Track;

public class TestCachedTrack {

	@Test
	public void shouldBeConvertedToJsonAndBack() throws Exception {
		CachedTrack expected = new CachedTrack();
		List<String> expectedProperties = Arrays.asList(new String[] { "name", "album", "artist", "bitRate",
				"downloadString", "duration", "fileFormat", "fileName", "genre", "hashcode", "sampleRate", "size", "year" });
		expected.setName("name");
		expected.setAlbum("album");
		expected.setArtist("artist");
		expected.setBitRate("bitRate");
		expected.setDownloadString("downloadString");
		expected.setDuration(100);
		expected.setFileFormat("mp3");
		expected.setFileName("filename.mp3");
		expected.setGenre("rock");
		expected.setHashcode("12345678");
		expected.setSampleRate("sampleRate");
		expected.setSize(1000);
		expected.setYear("1984");

		Track actual = JsonConverter.toBean(JsonConverter.toJson(expected), RemoteTrack.class);
		compareTracks(expected, actual, expectedProperties);

		actual = JsonConverter.toBean(JsonConverter.toJson(expected), RemoteTrack.class);
	}

	private void compareTracks(Track expected, Track actual, List<String> expectedProperties)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		for (String property : expectedProperties) {
			Object expectedProp = PropertyUtils.getProperty(expected, property);
			Object actualProp = PropertyUtils.getProperty(actual, property);

			assertEquals(expectedProp, actualProp);
		}
	}

}
