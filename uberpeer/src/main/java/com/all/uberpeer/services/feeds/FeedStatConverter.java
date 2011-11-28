package com.all.uberpeer.services.feeds;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.all.backend.commons.newsfeed.DataFeed;
import com.all.shared.json.JsonConverter;
import com.all.shared.newsfeed.AbstractFeed;
import com.all.shared.newsfeed.AllFeed;
import com.all.shared.stats.FeedStat;

@Component
public class FeedStatConverter implements Stat2FeedConverter<FeedStat> {
	@Autowired
	private AllFeed2DataFeedConverter manager;

	@Override
	public DataFeed convert(FeedStat feedStat) {
		AllFeed allFeed = JsonConverter.toBean(feedStat.getJson(), AllFeed.class);
		if (allFeed instanceof AbstractFeed) {
			((AbstractFeed) allFeed).updateTimestamp();
		}
		return manager.convert(allFeed);
	}

	@Override
	public Class<FeedStat> getStatClass() {
		return FeedStat.class;
	}

}
