package com.all.rds.loader.util;

import java.io.File;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v24Tag;

public class JAudioTaggerMetadataReader implements MetadataReader {

	private static final Log LOGGER = LogFactory.getLog(JAudioTaggerMetadataReader.class);

	public JAudioTaggerMetadataReader() {
		turnOffLogMessages();
	}

	private void turnOffLogMessages() {
		try {
			Handler[] handlers = Logger.getLogger("").getHandlers();
			for (int index = 0; index < handlers.length; index++) {
				handlers[index].setLevel(Level.OFF);
			}
		} catch (Exception e) {
			LOGGER.warn("Could not disable JAudioTagger logs.");
		}
	}

	@Override
	public Metadata read(File file) {
		try {
			AudioFile audioFile = AudioFileIO.read(file);
			Tag tag = getTag(audioFile);
			return getMetadata(file, audioFile, tag);
		} catch (Exception e) {
			LOGGER.error("Could not read metadata for " + file, e);
			return null;
		}
	}

	private Tag getTag(AudioFile audioFile) {
		Tag tag = null;
		if (audioFile instanceof MP3File) {
			MP3File mp3AudioFile = (MP3File) audioFile;
			if (mp3AudioFile.hasID3v1Tag() || mp3AudioFile.hasID3v2Tag()) {
				tag = mp3AudioFile.getTag();
			} else {
				tag = new ID3v24Tag();
				mp3AudioFile.setID3v2TagOnly((AbstractID3v2Tag) tag);
				try {
					mp3AudioFile.commit();
				} catch (CannotWriteException ignore) {
				}
			}
		} else {
			tag = audioFile.getTag();
		}
		return tag;
	}

	private Metadata getMetadata(File file, AudioFile audioFile, Tag tag) {
		MetadataImpl metadata = new MetadataImpl();
		AudioHeader header = audioFile.getAudioHeader();
		metadata.setBitRate(header.getBitRate());
		metadata.setSampleRate(header.getSampleRate().trim());
		metadata.setDuration(header.getTrackLength());
		metadata.setArtist(tag.getFirst(FieldKey.ARTIST));
		metadata.setAlbum(tag.getFirst(FieldKey.ALBUM));
		String genre = tag.getFirst(FieldKey.GENRE);
		if (genre.contains("(")) {
			genre = genre.substring(1, genre.length());
			genre = genre.substring(0, genre.lastIndexOf(")"));
			metadata.setGenre(com.all.rds.loader.util.GenreTypes.getGenreByCode(Integer.valueOf(genre).intValue()).getName());
		} else {
			metadata.setGenre(genre);
		}
		metadata.setBitRate(getBitrate(audioFile.getAudioHeader()));
		String year = tag.getFirst(FieldKey.YEAR);
		if (year.length() > 4) {
			year = year.substring(0, 4);
		}
		metadata.setYear(year);
		String name = tag.getFirst(FieldKey.TITLE);
		if (name == null || name.isEmpty()) {
			name = file.getName().substring(0, file.getName().lastIndexOf('.')).trim();
		}
		metadata.setName(name);
		metadata.setTrackNumber(getTrackNumber(audioFile, tag));
		metadata.setFile(file);
		return metadata;
	}

	public String getBitrate(AudioHeader header) {
		String bitRate = "";
		try {
			bitRate = header.getBitRate();
			if (bitRate.startsWith("~")) {
				bitRate = bitRate.substring(1); // removes the ~
			}
		} catch (UnsupportedOperationException e) {
			return "";
		}
		return bitRate;
	}

	private String getTrackNumber(AudioFile audioFile, Tag tag) {
		String trackNumber = "";
		try {
			if (audioFile.toString().contains("Flac")) {
				trackNumber = tag.getFirstField("TRCK").toString().trim();
			} else {
				// TrackNumber is not supported in ID3Tag version 1.0
				trackNumber = tag.getFirst(FieldKey.TRACK).trim();
			}
		} catch (Exception e) {
		}
		return trackNumber;
	}
}
