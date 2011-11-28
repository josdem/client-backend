package com.all.uberpeer.services.feeds;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.all.backend.commons.media.MusicTrack;
import com.all.backend.commons.newsfeed.DataFeed;
import com.all.backend.commons.newsfeed.Feed;
import com.all.backend.commons.newsfeed.FeedSubscriber;
import com.all.backend.commons.newsfeed.FeedSubscriber.FeedStatus;
import com.all.shared.model.ContactInfo;
import com.all.shared.model.FeedTrack;
import com.all.shared.newsfeed.PostedListeningTrackTwitterFeed;
import com.all.shared.stats.PostedListeningTrackTwitterStat;
import com.all.uberpeer.persistence.ContactDao;
import com.all.uberpeer.persistence.TrackDao;

@Component
public class PostedListeningTrackTwitterStatConverter implements Stat2FeedConverter<PostedListeningTrackTwitterStat>{

	@Autowired
	private ContactDao contactDao;
	@Autowired
	private TrackDao trackDao;
	@Override
	public DataFeed convert(PostedListeningTrackTwitterStat postedListeningTrackTwitterStat) {
		String hashcode = postedListeningTrackTwitterStat.getHashcode();
		ContactInfo owner = contactDao.findContactByEmail(postedListeningTrackTwitterStat.getEmail());
		MusicTrack track = trackDao.findTrackByHashcode(hashcode);
		
		FeedTrack feedTrack = new FeedTrack(hashcode, track.getName(), track.getArtist());
		PostedListeningTrackTwitterFeed postedListeningTrackTwitterFeed = new PostedListeningTrackTwitterFeed(owner, feedTrack);
		
		Feed feed = new Feed(postedListeningTrackTwitterFeed);
		
		FeedSubscriber subscriber = new FeedSubscriber(owner.getId(), FeedStatus.DEFAULT);
		return new DataFeed(feed, subscriber);
	}

	@Override
	public Class<PostedListeningTrackTwitterStat> getStatClass() {
		return PostedListeningTrackTwitterStat.class;
	}

}
