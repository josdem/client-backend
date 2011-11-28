package com.all.uberpeer.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.all.backend.commons.library.LibraryDelta;
import com.all.backend.commons.library.LibrarySnapshot;
import com.all.backend.commons.library.LibrarySyncStatus;
import com.all.messengine.MessEngine;
import com.all.messengine.impl.StubMessEngine;
import com.all.shared.model.AllMessage;
import com.all.shared.model.SyncValueObject;
import com.all.uberpeer.UberpeerConstants;
import com.all.uberpeer.persistence.LibraryDao;

public class TestLibraryService {

	@InjectMocks
	private LibraryService libraryService = new LibraryService();
	@Mock
	private ScheduledExecutorService syncExecutor;
	@Mock
	private LibraryDao libraryDao;
	@Captor
	private ArgumentCaptor<Runnable> syncTaskCaptor;

	private MessEngine messEngine = new StubMessEngine();

	private String firstAddEncodedDelta = "XQAAgAC5AgAAAAAAAAAtnsBGU66wVL/OjiG9ixR7MlZy1kRUkceRGlqjXvnZS0DhSdV45V7pRpD9rID247UsoC+2Hyp5fsR/+LLYr3cNc6LR5ex2Q8Ap0kX6UlkxnohHO1WeiLxF/3zKTtQtT5ujmGFiRSP+YNgQ2jjviJoK7XZkB7tr09xgZcDF0EVIVz5R8jjybdS5GgUskfCtTkdZ6M9lKRpTG+AM0RnUp3rPA8LmYvINtcPmNBKyPxcp3TjQdnypj8enZNRHY4pFMO4jnq7Hskov4bqjX4gZhuQ0PXRL+2usMAcM31bIeXlbg7BpZB/BSDxtvBRjATm1gRtdGCUMTUKaM2Dj+WzhpMpXCCKWeElU8pDXGbM/1qkQWOWGNm2rx/ZSGriHKy3yr33p/9uktRqX69KoXgRNfBOoyN55jdtir7MJtrkIayehwp0V/m2q+OFQDAuhHHExEd2UHpATuGnjh7B1dK3u22i8uyot2VxgvgGRc8LqaNERIZduczeZL55Fa330mX0x1HhPL6aHR4f4D0L8X8HYkx1jwTcA";
	private String secondAddEncodedDelta = "XQAAgABxBQAAAAAAAAAtnsBGU66wVL/OjiG9ixR7MlZy1kRUkceRGlqjXvnZS0DhSdV45V7pRpD9rID247UsoC+2Hyp5fsR/+LLYr3cNc6LR5ex2Q8Ap0kX6UlkxnohHO1WeiLxF/3zKTtQtT5ujmGFiRSP+YNgQ2jjviJoK7XZkB7tr09xgZcDF0EVIVz5R8jjybdS5GgUskfCtTkdZ6M9lKRpTG+AM0RnUp3rPA8LmYvINtcPmNBKyPxcp3TjQdnypj8enZNRHY4pFMO4jnq7Hskov4bqjX4gZhuQ0PXRL+2usMAcM31bIeXlbg7BpZB/BSDxtvBRjATm1gRtdGCUMTUKaM2Dj+WzhpMpXCCKWeElU8pDXGbM/1qkQWOWGNm2rx/ZSGriHKy3yr33p/9uktRqX69KoXgRNfBOoyN55jdtir7MJtrkIayehwp0V/m2q+OFQDAuhHHExEd2UHpATuGnjh7B1dK3u22i8uyot2VxgvgGRc8LqaNERIZduczeZL55Fa330mX0x1HhPL6aHR4Kn/I1EfHDHIM1NV97yYVnh9iDZfKVqd56ikrTy1QaoyMaUqFnWLXqQVd+fi1jmXfK6orK2XOy9/OpY5LR0AiRHgAXzEcYPkGAT9Z/mxw1DGToE6KcpEKOuhGS6m47ZvsJ62y330bwBPwOg1K4UXRPkqLkNUZFbTC3tBpdRTs9drjxkfkKm46Lf85HgiEo3QFtBLIF2Mn/TUVa8JYTcU8fHIgAA";
	private String updateEncodedDelta = "XQAAgAB/AQAAAAAAAAAtnsBGU66wVL/OjiG9ixjchSyO4Y+WqkDHE+/8UOO1OIlen5H3P+l2Hz54Hu1FrwcvfXvtaAhaB0Ztr91Ped5mEvKDs8hWaCIxTUU1zUSCImAt+5T0HalR+cakaL2emVmHO5DFFejY4jPTS25C84EmeE5wn3cykoQFp8E9RunMgMXMt3VuJaMrg62Bn8byI1dcQcBxstb93lWy8LjKUJB7YAcLajT8kY6PfhrsFGr3K7tKkhGponCGfw1s7lxYoFt0ST2ll5JU85fOpUNYllflmeJZLEcKyn605t0akXfT72GdYuAob3UuLs08L90AAA";

	private final String owner = "user@all.com";
	private final int currentSnapshot = 1;
	private final int nextSnapshot = 2;
	private final int currentDelta = 5;
	private final int nextDelta = 6;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		((StubMessEngine) messEngine).setup(libraryService);
	}

	@Test
	public void shouldStart() throws Exception {
		messEngine.send(new AllMessage<Integer>(UberpeerConstants.START_UBERPEER_SERVICES_TYPE, 10000));
		verify(syncExecutor).schedule(syncTaskCaptor.capture(), anyLong(), eq(TimeUnit.MINUTES));
	}

	@Test
	public void shouldStop() {
		messEngine.send(new AllMessage<Void>(UberpeerConstants.STOP_UBERPEER_SERVICES_TYPE, null));
		verify(syncExecutor).shutdownNow();
		when(syncExecutor.isShutdown()).thenReturn(true);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldExecuteSyncTaskEndlesslyWhileServiceRunning() throws Exception {
		shouldStart();
		Runnable syncTask = syncTaskCaptor.getValue();
		assertNotNull(syncTask);
		reset(syncExecutor);

		when(libraryDao.findUnprocessedLibraries()).thenReturn(Collections.EMPTY_LIST);
		syncTask.run();
		verify(syncExecutor).schedule(eq(syncTask), anyLong(), eq(TimeUnit.MINUTES));
		reset(syncExecutor);

		when(libraryDao.findUnprocessedLibraries()).thenThrow(new RuntimeException("some unexpected exception"));
		syncTask.run();
		verify(syncExecutor).schedule(eq(syncTask), anyLong(), eq(TimeUnit.MINUTES));
		reset(syncExecutor);

		shouldStop();
		syncTask.run();
		verify(syncExecutor, never()).schedule(eq(syncTask), anyLong(), eq(TimeUnit.MINUTES));
	}

	@Test
	@Ignore("work in progress")
	public void shouldUpdateSnapshotAndDelta() throws Exception {
		shouldStart();
		Runnable syncTask = syncTaskCaptor.getValue();

		LibrarySyncStatus librarySyncStatus = prepareSnapshotAndDeltas(currentSnapshot, currentDelta);

		SyncValueObject snapshotValue = getSnapshotValueFromEvent(firstAddEncodedDelta, currentSnapshot);

		LibrarySnapshot snapshot = new LibrarySnapshot(snapshotValue);
		when(libraryDao.findSnapshotByUserAndVersion(librarySyncStatus.getOwner(), librarySyncStatus.getCurrentSnapshot()))
				.thenReturn(snapshot);
		List<LibraryDelta> libraryDeltas = new ArrayList<LibraryDelta>();
		libraryDeltas.add(new LibraryDelta(new Long(currentSnapshot), snapshotValue));

		when(libraryDao.findDeltasBySnapshot(new Long(currentSnapshot), 0)).thenReturn(libraryDeltas);

		syncTask.run();
		assertEquals(currentDelta, librarySyncStatus.getCurrentDelta());
		verify(libraryDao, times(3)).update(librarySyncStatus);
		// verify(libraryDao).saveOrUpdate(isA(MusicTrack.class));
		assertEquals(currentSnapshot, librarySyncStatus.getProcessedSnapshot());
		assertEquals(currentDelta, librarySyncStatus.getProcessedDelta());

		LibrarySyncStatus laterLibrarySyncStatus = prepareSnapshotAndDeltas(nextSnapshot, nextDelta);
		SyncValueObject secondSnapshotValue = getSnapshotValueFromEvent(secondAddEncodedDelta, nextSnapshot);
		LibrarySnapshot secondSnapshot = new LibrarySnapshot(secondSnapshotValue);
		List<LibraryDelta> nextLibraryDeltas = new ArrayList<LibraryDelta>();
		libraryDeltas.add(new LibraryDelta(new Long(nextSnapshot), secondSnapshotValue));
		when(libraryDao.findSnapshotByUserAndVersion(librarySyncStatus.getOwner(), librarySyncStatus.getCurrentSnapshot())).thenReturn(secondSnapshot);
		when(libraryDao.findDeltasBySnapshot(new Long(nextSnapshot), nextDelta)).thenReturn(nextLibraryDeltas);
		List<LibrarySyncStatus> unprocessedLibraries = new ArrayList<LibrarySyncStatus>();
		unprocessedLibraries.add(laterLibrarySyncStatus);
		when(libraryDao.findUnprocessedLibraries()).thenReturn(unprocessedLibraries);
		syncTask.run();
		assertEquals(nextSnapshot, laterLibrarySyncStatus.getProcessedSnapshot());

	}

	private SyncValueObject getSnapshotValueFromEvent(String firstAddEncodedDelta, int currentSnapshot) {
		SyncValueObject snapshotValue = new SyncValueObject(owner, currentSnapshot, 0, System.currentTimeMillis());
		List<String> events = new ArrayList<String>();
		events.add(firstAddEncodedDelta);
		snapshotValue.setEvents(events);
		return snapshotValue;
	}

	private LibrarySyncStatus prepareSnapshotAndDeltas(int currentSnapshot, int currentDelta) {
		List<LibrarySyncStatus> libraryStatuses = new ArrayList<LibrarySyncStatus>();
		LibrarySyncStatus librarySyncStatus = new LibrarySyncStatus(owner, currentSnapshot, currentDelta);
		libraryStatuses.add(librarySyncStatus);
		when(libraryDao.findUnprocessedLibraries()).thenReturn(libraryStatuses);
		return librarySyncStatus;
	}

	@Test
	public void shouldUpdateDeltas() throws Exception {
		shouldStart();
		Runnable syncTask = syncTaskCaptor.getValue();

		LibrarySyncStatus librarySyncStatus = prepareSnapshotAndDeltas(currentSnapshot, currentDelta);
		SyncValueObject snapshotValue = new SyncValueObject(owner, currentSnapshot, 0, System.currentTimeMillis());
		List<String> events = new ArrayList<String>();
		events.add(updateEncodedDelta);
		snapshotValue.setEvents(events);
		LibrarySnapshot snapshot = new LibrarySnapshot(snapshotValue);
		when(libraryDao.findSnapshotByUserAndVersion(librarySyncStatus.getOwner(), librarySyncStatus.getCurrentSnapshot())).thenReturn(snapshot);
		syncTask.run();
		verify(libraryDao, times(3)).update(librarySyncStatus);
	}

}
