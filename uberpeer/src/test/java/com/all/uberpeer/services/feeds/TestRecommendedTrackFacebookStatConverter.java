package com.all.uberpeer.services.feeds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.all.backend.commons.media.MusicTrack;
import com.all.backend.commons.newsfeed.DataFeed;
import com.all.backend.commons.newsfeed.Feed;
import com.all.backend.commons.newsfeed.FeedSubscriber.FeedStatus;
import com.all.shared.model.ContactInfo;
import com.all.shared.newsfeed.FeedType;
import com.all.shared.stats.RecommendedTrackFacebookStat;
import com.all.uberpeer.persistence.ContactDao;
import com.all.uberpeer.persistence.TrackDao;

public class TestRecommendedTrackFacebookStatConverter {

	@InjectMocks
	private RecommendedTrackFacebookStatConverter converter = new RecommendedTrackFacebookStatConverter();
	@Mock
	private ContactDao contactDao;
	@Mock
	private TrackDao trackDao;
	@SuppressWarnings("deprecation")
	private ContactInfo contactInfo = new ContactInfo();

	private MusicTrack musicTrack = new MusicTrack();

	private String email = "email";

	private String hashcode = "hashcode";

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		contactInfo.setEmail(email);
		contactInfo.setId(7L);
	}

	@Test
	public void shouldConvertStatToFeed() throws Exception {
		when(contactDao.findContactByEmail(email)).thenReturn(contactInfo);
		when(trackDao.findTrackByHashcode(hashcode)).thenReturn(musicTrack);

		RecommendedTrackFacebookStat recommendedTrackFacebookStat = new RecommendedTrackFacebookStat(email, hashcode);

		DataFeed dataFeed = converter.convert(recommendedTrackFacebookStat);

		Feed feed = dataFeed.getFeed();
		assertNotNull(dataFeed);
		assertNotNull(feed);
		assertEquals(1, dataFeed.getSubscribers().size());
		assertEquals(contactInfo.getId(), feed.getOwner());
		assertEquals(FeedType.RECOMMENDED_TRACK_FACEBOOK, feed.getType().intValue());
		assertNotNull(feed.getBody());
		assertEquals(contactInfo.getId(), dataFeed.getSubscribers().get(0).getSubscriberId());
		assertEquals(FeedStatus.DEFAULT, dataFeed.getSubscribers().get(0).getStatus());
	}

}
