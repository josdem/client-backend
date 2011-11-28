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
import com.all.shared.newsfeed.RecommendedTrackTwtterFeed;
import com.all.shared.stats.RecommendedTrackTwitterStat;
import com.all.uberpeer.persistence.ContactDao;
import com.all.uberpeer.persistence.TrackDao;

@Component
public class RecommendedTrackTwitterStatConverter implements Stat2FeedConverter<RecommendedTrackTwitterStat> {

	@Autowired
	private ContactDao contactDao;
	@Autowired
	private TrackDao trackDao;

	@Override
	public DataFeed convert(RecommendedTrackTwitterStat recommendedTrackTwitterStat) {
		String hashcode = recommendedTrackTwitterStat.getHashcode();
		ContactInfo owner = contactDao.findContactByEmail(recommendedTrackTwitterStat.getEmail());
		MusicTrack track = trackDao.findTrackByHashcode(hashcode);
		FeedTrack feedTrack = new FeedTrack(hashcode, track.getName(), track.getArtist());
		RecommendedTrackTwtterFeed recommendedTrackTwitterFeed = new RecommendedTrackTwtterFeed(owner, feedTrack);
		Feed feed = new Feed(recommendedTrackTwitterFeed);

		FeedSubscriber subscriber = new FeedSubscriber(owner.getId(), FeedStatus.DEFAULT);

		return new DataFeed(feed, subscriber);
	}

	@Override
	public Class<RecommendedTrackTwitterStat> getStatClass() {
		return RecommendedTrackTwitterStat.class;
	}

}
