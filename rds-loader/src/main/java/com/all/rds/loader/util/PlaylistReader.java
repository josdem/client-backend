package com.all.rds.loader.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.all.rds.model.TopPlaylist;

public class PlaylistReader {

	private static final Comparator<File> FILE_NAME_COMPARATOR = new Comparator<File>() {
		@Override
		public int compare(File fileA, File fileB) {
			String trackNumA = fileA.getName().split(" ")[0];
			String trackNumB = fileB.getName().split(" ")[0];
			if (StringUtils.isNumeric(trackNumA) && StringUtils.isNumeric(trackNumB)) {
				return new Integer(trackNumA).compareTo(new Integer(trackNumB));
			}
			if(StringUtils.isNumeric(trackNumA)){
				return 1;
			}
			if(StringUtils.isNumeric(trackNumB)){
				return -1;
			}
			return fileA.getName().compareTo(fileB.getName());
		}
	};
	private final TopPlaylist playlist;
	private List<File> tracks;

	public PlaylistReader(File playlistDir) {
		playlist = new TopPlaylist(playlistDir.getName());
		tracks = new ArrayList<File>();
		for (File file : FileUtil.listAudioFiles(playlistDir, false)) {
			if (!file.isHidden()) {
				tracks.add(file);
			}
		}
		Collections.sort(tracks, FILE_NAME_COMPARATOR);
	}

	public TopPlaylist getPlaylist() {
		return playlist;
	}

	public List<File> getTracks() {
		return tracks;
	}

}
