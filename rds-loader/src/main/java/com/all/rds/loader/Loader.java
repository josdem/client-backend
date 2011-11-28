package com.all.rds.loader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.all.rds.loader.util.FileUtil;
import com.all.rds.loader.util.PlaylistReader;
import com.all.rds.loader.util.TrackReader;
import com.all.rds.model.TopPlaylist;
import com.all.rds.model.TopPlaylistTrack;
import com.all.rds.model.TrackUploadStatus;
import com.all.shared.mc.TrackStatus;

public class Loader {

	private final Log log = LogFactory.getLog(this.getClass());

	private final HibernateTemplate hibernateTemplate;

	public Loader(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}

	public void loadAllTracks(File baseDir) throws IllegalArgumentException {
		if (FileUtil.isValidDir(baseDir)) {
			List<File> audioFiles = FileUtil.listAudioFiles(baseDir, true);
			log.info("Will load " + audioFiles.size() + " tracks found in " + baseDir);
			loadTracks(audioFiles);
		}
	}

	public List<String> loadAllPlaylists(File baseDir) {
		long startTime = System.currentTimeMillis();
		List<String> loadedPlaylists = new ArrayList<String>();
		if (FileUtil.isValidDir(baseDir)) {
			File[] playlistDirs = baseDir.listFiles();
			log.info("Will load playlists found in " + baseDir);
			for (File playlistDir : playlistDirs) {
				if (FileUtil.isValidDir(playlistDir)) {
					log.info("Loading playlist " + playlistDir.getName());
					String playlist = loadPlaylist(playlistDir);
					if (playlist != null) {
						loadedPlaylists.add(playlist);
					}
				}
			}
		} else {
			throw new IllegalArgumentException("The path provided is not a directory.");
		}
		log.info("It took " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime) + " seconds to load "
				+ loadedPlaylists.size() + " playlists.");
		return loadedPlaylists;
	}

	private String loadPlaylist(File playlistDir) {
		String hashcode = null;
		try {
			PlaylistReader reader = new PlaylistReader(playlistDir);
			TopPlaylist playlist = reader.getPlaylist();
			hibernateTemplate.saveOrUpdate(playlist);
			List<String> importedTracks = loadTracks(reader.getTracks());
			List<TopPlaylistTrack> topPlaylistTracks = new ArrayList<TopPlaylistTrack>();
			int counter = 0;
			for (String track : importedTracks) {
				topPlaylistTracks.add(new TopPlaylistTrack(playlist.getHashcode(), track, counter));
				counter++;
			}
			hibernateTemplate.saveOrUpdateAll(topPlaylistTracks);
			hashcode = playlist.getHashcode();
		} catch (Exception e) {
			log.error("Could not load playlist from " + playlistDir.getName(), e);
		}
		return hashcode;
	}

	private List<String> loadTracks(List<File> files) {
		List<String> savedHashcodes = new ArrayList<String>();
		for (File file : files) {
			try {
				savedHashcodes.add(loadTrack(file));
			} catch (Exception e) {
				log.error("Could not load track " + file, e);
			}
		}
		log.info(savedHashcodes.size() + " tracks were loaded.");
		return savedHashcodes;
	}

	private String loadTrack(File file) throws IllegalArgumentException, IOException {
		TrackReader trackReader = new TrackReader(file);
		if (!isTrackImported(trackReader.getHashcode())) {
			hibernateTemplate.saveOrUpdateAll(trackReader.getTrackEntities());
		} else {
			log.info("Skipping track " + file.getName() + " since it is already loaded.");
		}
		return trackReader.getHashcode();
	}

	private boolean isTrackImported(String hashcode) {
		TrackUploadStatus currentStatus = hibernateTemplate.get(TrackUploadStatus.class, hashcode);
		return currentStatus != null && (TrackStatus.Status.UPLOADED == currentStatus.getTrackStatus());
	}

	private static boolean parseArgs(String[] args) {
		boolean importPlaylists = true;
		if (args.length - 1 < 0) {
			throw new IllegalArgumentException("Base directory argument is missing. Use java Loader {baseDirectory}");
		}
		if (args.length >= 2) {
			if (args[0].equals("-t")) {
				importPlaylists = false;
			} else {
				throw new IllegalArgumentException("Unrecognized argument " + args[0]);
			}
		}
		return importPlaylists;
	}

	public static void main(String[] args) {
		boolean isPlaylistOpt = parseArgs(args);
		ClassPathXmlApplicationContext appCtx = new ClassPathXmlApplicationContext("applicationContext.xml");
		appCtx.refresh();
		HibernateTemplate hibernateTemplate = (HibernateTemplate) appCtx.getBean("hibernateTemplate");
		Loader loader = new Loader(hibernateTemplate);
		File baseDir = new File(args[args.length - 1]);
		if (isPlaylistOpt) {
			loader.loadAllPlaylists(baseDir);
		} else {
			loader.loadAllTracks(baseDir);
		}
	}
}
