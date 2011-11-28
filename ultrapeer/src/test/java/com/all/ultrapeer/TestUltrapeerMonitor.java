package com.all.ultrapeer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.all.messengine.impl.StubMessEngine;
import com.all.shared.model.AllMessage;
import com.all.ultrapeer.messages.UltrapeerMessages;

public class TestUltrapeerMonitor {

	@InjectMocks
	private UltrapeerMonitor ultrapeerMonitor = new UltrapeerMonitor();
	@Mock
	private UltrapeerConfig ultrapeerConfig;
	@Mock
	private ScheduledExecutorService monitor;
	@Captor
	private ArgumentCaptor<Runnable> runnableCaptor;

	private StubMessEngine messEngine = new StubMessEngine();
	private Long monitorDelay = 5l;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		messEngine.setup(ultrapeerMonitor);
		when(ultrapeerConfig.getTypedProperty(anyString(), eq(Long.class))).thenReturn(monitorDelay);
	}

	@Test
	public void shouldStart() throws Exception {
		messEngine.send(new AllMessage<Void>(UltrapeerMessages.Types.START_ULTRAPEER_SERVICES_TYPE, null));

		verify(monitor).scheduleWithFixedDelay(runnableCaptor.capture(), eq(0L), eq(monitorDelay), eq(TimeUnit.SECONDS));
	}

	@Test
	public void shouldStop() throws Exception {
		messEngine.send(new AllMessage<Void>(UltrapeerMessages.Types.STOP_ULTRAPEER_SERVICES_TYPE, null));

		verify(monitor).shutdownNow();
	}

	@Test
	@Ignore("it is flaky on hudson")
	public void shouldReportUnhealthyStatusBecauseOfCpuUsage() throws Exception {
		shouldStart();
		Double cpuTreshold = 1.0;
		Double heapTreshold = 80.0;
		when(ultrapeerConfig.getTypedProperty("ultrapeer_cpu_treshold", Double.class)).thenReturn(cpuTreshold);
		when(ultrapeerConfig.getTypedProperty("ultrapeer_heap_treshold", Double.class)).thenReturn(heapTreshold);

		Thread thread = newExpensiveCpuThread();
		thread.start();
		// The first time cpu is higher than expected it is ignored since it may
		// false
		runnableCaptor.getValue().run();
		assertTrue(ultrapeerMonitor.isHealthy());
		// The second time it should be reported as a problem
		runnableCaptor.getValue().run();
		assertFalse(ultrapeerMonitor.isHealthy());
		thread.interrupt();
	}

	/**
	 * @throws Exception
	 */
	@Test
	@Ignore("it is flaky on hudson")
	public void shouldReportUnhealthyStatusBecauseOfHeapUsage() throws Exception {
		shouldStart();
		Double cpuTreshold = 80.0;
		Double heapTreshold = 20.0;
		when(ultrapeerConfig.getTypedProperty("ultrapeer_cpu_treshold", Double.class)).thenReturn(cpuTreshold);
		when(ultrapeerConfig.getTypedProperty("ultrapeer_heap_treshold", Double.class)).thenReturn(heapTreshold);

		final AtomicBoolean done = new AtomicBoolean(false);
		final List<String> list = new LinkedList<String>();
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < Integer.MAX_VALUE; i++) {
					list.add("" + i);
					if (Runtime.getRuntime().freeMemory() < (1024 * 100)) {
						break;
					}
				}
				done.set(true);

			}
		});
		thread.start();
		while (!done.get()) {
			runnableCaptor.getValue().run();
			if (!ultrapeerMonitor.isHealthy()) {
				thread.interrupt();
				break;
			}
		}
		assertFalse(done.get());
	}

	@Test
	public void shouldReportHealthyStatus() throws Exception {
		shouldStart();
		Double healthyTreshold = 100.0;
		when(ultrapeerConfig.getTypedProperty(anyString(), eq(Double.class))).thenReturn(healthyTreshold);

		runnableCaptor.getValue().run();
		assertTrue(ultrapeerMonitor.isHealthy());
		runnableCaptor.getValue().run();
		assertTrue(ultrapeerMonitor.isHealthy());
		runnableCaptor.getValue().run();
		assertTrue(ultrapeerMonitor.isHealthy());
	}

	private Thread newExpensiveCpuThread() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				int i = 0;
				while (i < Integer.MAX_VALUE - 1) {
					i++;
				}
			}
		});
		return thread;
	}

}
