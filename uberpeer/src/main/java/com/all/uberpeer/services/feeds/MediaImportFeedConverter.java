package com.all.uberpeer.services.feeds;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.all.backend.commons.newsfeed.DataFeed;
import com.all.backend.commons.newsfeed.Feed;
import com.all.backend.commons.newsfeed.FeedSubscriber;
import com.all.backend.commons.newsfeed.FeedSubscriber.FeedStatus;
import com.all.shared.model.ContactInfo;
import com.all.shared.newsfeed.MediaImportFeed;
import com.all.shared.stats.MediaImportStat;
import com.all.shared.stats.usage.UserActions;
import com.all.uberpeer.persistence.ContactDao;

@Component
public class MediaImportFeedConverter implements Stat2FeedConverter<MediaImportStat> {

	@Autowired
	private ContactDao contactDao;

	@Override
	public DataFeed convert(MediaImportStat stat) {
		ContactInfo owner = contactDao.findContactByEmail(stat.getEmail());
		MediaImportFeed mediaFeed = new MediaImportFeed(owner, stat
				.getTotalTracks(), stat.getTotalPlaylists(), stat.getTotalFolders());
		if (UserActions.Player.IMPORT_MEDIA_ITUNES == stat.getImportTypeAction()) {
			mediaFeed.setFromItunes(true);
		}
		Feed feed = new Feed(mediaFeed);
		return new DataFeed(feed, Arrays.asList(new FeedSubscriber(owner.getId(), FeedStatus.DEFAULT)));
	}

	@Override
	public Class<MediaImportStat> getStatClass() {
		return MediaImportStat.class;
	}

}
