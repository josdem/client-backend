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
import com.all.shared.model.ContactInfo;
import com.all.shared.newsfeed.FeedType;
import com.all.shared.stats.RecommendedTrackTwitterStat;
import com.all.uberpeer.persistence.ContactDao;
import com.all.uberpeer.persistence.TrackDao;

public class TestRecommendedTrackTwitterStatConverter {

	@InjectMocks
	private RecommendedTrackTwitterStatConverter recommendedTrackTwitterFeedConverter = new RecommendedTrackTwitterStatConverter();

	private static final String HASHCODE = "hashcode";
	private static final String EMAIL = "email";

	private ContactInfo contactInfo;

	private MusicTrack musicTrack;

	@Mock
	private ContactDao contactDao;

	@Mock
	private TrackDao trackDao;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		initReturnObjects();
		when(contactDao.findContactByEmail(EMAIL)).thenReturn(contactInfo);
		when(trackDao.findTrackByHashcode(HASHCODE)).thenReturn(musicTrack);
	}

	@SuppressWarnings("deprecation")
	private void initReturnObjects() {
		contactInfo = new ContactInfo();
		contactInfo.setEmail(EMAIL);
		musicTrack = new MusicTrack();
		musicTrack.setHashcode(HASHCODE);
	}

	@Test
	public void shouldConvertStatToFeed() throws Exception {
		RecommendedTrackTwitterStat recommendedTrackTwitterStat = new RecommendedTrackTwitterStat(EMAIL, HASHCODE);

		DataFeed dataFeed = recommendedTrackTwitterFeedConverter.convert(recommendedTrackTwitterStat);

		Feed feed = dataFeed.getFeed();

		assertNotNull(dataFeed);
		assertNotNull(feed);

		assertEquals(1, dataFeed.getSubscribers().size());
		assertEquals(contactInfo.getId(), feed.getOwner());
		assertEquals(FeedType.RECOMMENDED_TRACK_TWITTER, feed.getType().intValue());
		assertNotNull(feed.getBody());

	}

}
