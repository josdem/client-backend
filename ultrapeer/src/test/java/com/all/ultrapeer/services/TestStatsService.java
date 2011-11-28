package com.all.ultrapeer.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.all.messengine.impl.StubMessEngine;
import com.all.shared.messages.MessEngineConstants;
import com.all.shared.model.AllMessage;
import com.all.shared.stats.AllStat;
import com.all.ultrapeer.util.AllServerProxy;

public class TestStatsService {
	@InjectMocks
	private StatsService statsService = new StatsService();
	@Spy
	private StubMessEngine stubEngine = new StubMessEngine();
	@Mock
	private AllServerProxy allServerProxy;
	@Mock
	private AllStat stat1;
	@Mock
	private AllStat stat2;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		stubEngine.setup(statsService);

	}

	@Test
	public void shouldSendStats() throws Exception {
		List<AllStat> stats = Arrays.asList(stat1);
		stubEngine.send(new AllMessage<Collection<AllStat>>(MessEngineConstants.USAGE_STATS_TYPE, stats));
		verify(allServerProxy).put("stats.put", stats);
	}

	@Test
	public void shouldNotSendEmptyStats() throws Exception {
		List<AllStat> stats = Collections.emptyList();
		stubEngine.send(new AllMessage<Collection<AllStat>>(MessEngineConstants.USAGE_STATS_TYPE, stats));
		verify(allServerProxy, never()).put("stats.put", stats);

	}

	@Test
	@SuppressWarnings("unchecked")
	public void shouldCacheIfNoConnectionToRest() throws Exception {
		doThrow(new RuntimeException()).when(allServerProxy).put(anyString(), any(Object.class));

		List<AllStat> stats = Arrays.asList(stat1);
		stubEngine.send(new AllMessage<Collection<AllStat>>(MessEngineConstants.USAGE_STATS_TYPE, stats));

		reset(allServerProxy);
		doNothing().when(allServerProxy).put(anyString(), any(Object.class));

		stats = Arrays.asList(stat2);
		stubEngine.send(new AllMessage<Collection<AllStat>>(MessEngineConstants.USAGE_STATS_TYPE, stats));

		ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
		verify(allServerProxy).put(eq("stats.put"), captor.capture());
		List value = captor.getValue();
		assertEquals(2, value.size());
		assertTrue(value.contains(stat1));
		assertTrue(value.contains(stat2));
	}
}
