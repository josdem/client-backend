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
import com.all.shared.newsfeed.RecommendedTrackFacebookFeed;
import com.all.shared.stats.RecommendedTrackFacebookStat;
import com.all.uberpeer.persistence.ContactDao;
import com.all.uberpeer.persistence.TrackDao;

@Component
public class RecommendedTrackFacebookStatConverter implements Stat2FeedConverter<RecommendedTrackFacebookStat> {

	@Autowired
	private ContactDao contactDao;

	@Autowired
	private TrackDao trackDao;

	
	@Override
	public DataFeed convert(RecommendedTrackFacebookStat recommendedTrackFacebookStat) {
		String hashcode = recommendedTrackFacebookStat.getHashcode();
		ContactInfo contactInfo = contactDao.findContactByEmail(recommendedTrackFacebookStat.getEmail());
		MusicTrack musicTrack = trackDao.findTrackByHashcode(hashcode);

		FeedTrack feedTrack = new FeedTrack(hashcode, musicTrack.getName(), musicTrack.getArtist());
		RecommendedTrackFacebookFeed recommendedTrackFacebookFeed = new RecommendedTrackFacebookFeed(contactInfo, feedTrack);

		Feed feed = new Feed(recommendedTrackFacebookFeed);
		FeedSubscriber feedSubscriber = new FeedSubscriber(contactInfo.getId(), FeedStatus.DEFAULT);
		return new DataFeed(feed, feedSubscriber);
	}

	@Override
	public Class<RecommendedTrackFacebookStat> getStatClass() {
		return RecommendedTrackFacebookStat.class;
	}

}
