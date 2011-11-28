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
import com.all.shared.stats.PostedListeningTrackTwitterStat;
import com.all.uberpeer.persistence.ContactDao;
import com.all.uberpeer.persistence.TrackDao;

public class TestPostedListeningTrackTwitterStatConverter {
    @InjectMocks
    private PostedListeningTrackTwitterStatConverter postedListeningTrackTwitterStatConverter = new PostedListeningTrackTwitterStatConverter();

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
		PostedListeningTrackTwitterStat posListeningTrackTwitterStat = new PostedListeningTrackTwitterStat(EMAIL, HASHCODE);

		DataFeed dataFeed = postedListeningTrackTwitterStatConverter.convert(posListeningTrackTwitterStat);

		Feed feed = dataFeed.getFeed();

		assertNotNull(dataFeed);
		assertNotNull(feed);

		assertEquals(1, dataFeed.getSubscribers().size());
		assertEquals(contactInfo.getId(), feed.getOwner());
		assertEquals(FeedType.POSTED_TRACK_TWITTER, feed.getType().intValue());
		assertNotNull(feed.getBody());

	}
}
