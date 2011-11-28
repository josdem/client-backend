package com.all.rds.loader.util;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;

public class FileUtil {

	private static final List<String> JAUDIOTAGGER_SUPPORTED_FORMATS = Arrays.asList(new String[] { "MP3", "M4A", "M4B",
			"OGG", "WMA", "WAV", "MP2", "AAC" });
	private static final IOFileFilter AUDIOFILE_FILTER = new SuffixFileFilter(JAUDIOTAGGER_SUPPORTED_FORMATS,
			IOCase.INSENSITIVE);

	public static boolean isValidDir(File dir) {
		return dir.exists() && dir.isDirectory();
	}

	@SuppressWarnings("unchecked")
	public static List<File> listAudioFiles(File dir, boolean recursive) {
		if(recursive){
			return (List<File>) FileUtils.listFiles(dir, AUDIOFILE_FILTER, FileFilterUtils.directoryFileFilter());			
		}
		return (List<File>) FileUtils.listFiles(dir, AUDIOFILE_FILTER, FileFilterUtils.fileFileFilter());
	}

}
