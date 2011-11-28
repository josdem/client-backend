package com.all.uberpeer.services.feeds;

import com.all.backend.commons.newsfeed.DataFeed;
import com.all.shared.stats.AllStat;

public interface Stat2FeedConverter<T extends AllStat> {
	DataFeed convert(T t);

	Class<T> getStatClass();
}
