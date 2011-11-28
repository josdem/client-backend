package com.all.uberpeer.services;

import static com.all.uberpeer.UberpeerConstants.START_UBERPEER_SERVICES_TYPE;
import static com.all.uberpeer.UberpeerConstants.STOP_UBERPEER_SERVICES_TYPE;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.limewire.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.all.backend.commons.library.LibraryDelta;
import com.all.backend.commons.library.LibrarySnapshot;
import com.all.backend.commons.library.LibrarySyncStatus;
import com.all.backend.commons.media.MusicTrack;
import com.all.messengine.MessageMethod;
import com.all.shared.model.SyncEventEntity;
import com.all.shared.model.SyncValueObject;
import com.all.shared.model.SyncEventEntity.SyncOperation;
import com.all.shared.sync.SyncGenericConverter;
import com.all.shared.util.SyncUtils;
import com.all.uberpeer.persistence.LibraryDao;

@Service
public class LibraryService {

	private static final String CLIENT_TRACK_ENTITY_TYPE = "LocalTrack";

	private static final long SYNC_TASK_ACTIVE_DELAY = 1;

	private static final TimeUnit SYNC_TAKS_DELAY_UNIT = TimeUnit.MINUTES;

	private final Log log = LogFactory.getLog(this.getClass());

	private final SyncTask syncTask = new SyncTask();

	private final ScheduledExecutorService syncExecutor = Executors.newSingleThreadScheduledExecutor();

	@Autowired
	private LibraryDao libraryDao;

	@MessageMethod(START_UBERPEER_SERVICES_TYPE)
	public void start() {
		log.info("Starting UberpeerLibraryService...");
		syncExecutor.schedule(syncTask, SYNC_TASK_ACTIVE_DELAY, SYNC_TAKS_DELAY_UNIT);
	}

	@MessageMethod(STOP_UBERPEER_SERVICES_TYPE)
	public void stop() {
		syncExecutor.shutdownNow();
	}

	private void updateLibrariesSyncStatus() {
		List<LibrarySyncStatus> unprocessedLibraries = libraryDao.findUnprocessedLibraries();
		for (LibrarySyncStatus librarySyncStatus : unprocessedLibraries) {
			updateLibrary(librarySyncStatus);
		}
	}

	private void updateLibrary(LibrarySyncStatus librarySyncStatus) {
		int fromDelta = librarySyncStatus.getProcessedDelta();
		LibrarySnapshot snapshot = libraryDao.findSnapshotByUserAndVersion(librarySyncStatus.getOwner(), librarySyncStatus
				.getCurrentSnapshot());
		if (librarySyncStatus.getProcessedSnapshot() < librarySyncStatus.getCurrentSnapshot()) {
			updateSnapshot(librarySyncStatus, snapshot);
			fromDelta = 0;
		}
		if (librarySyncStatus.getProcessedDelta() != librarySyncStatus.getCurrentDelta()) {
			librarySyncStatus.setProcessedDelta(fromDelta);
			updateDeltas(librarySyncStatus, snapshot.getId());
		}
		libraryDao.update(librarySyncStatus);
	}

	private void updateSnapshot(LibrarySyncStatus librarySyncStatus, LibrarySnapshot snapshot) {
		SyncValueObject snapshotValue = snapshot.getSnapshot();
		for (String encodedEvents : snapshotValue.getEvents()) {
			decodeAndProcessEvents(encodedEvents);
		}
		librarySyncStatus.setProcessedSnapshot(librarySyncStatus.getCurrentSnapshot());
		libraryDao.update(librarySyncStatus);
	}

	private void decodeAndProcessEvents(String encodedEvents) {
		List<SyncEventEntity> syncEvents = SyncUtils.decodeAndUnzip(encodedEvents);
		for (SyncEventEntity syncEvent : syncEvents) {
			processSyncEvent(syncEvent);
		}
	}

	private void processSyncEvent(SyncEventEntity syncEvent) {
		Map<String, Object> attributes = syncEvent.getEntity();
		if (isSaveEvent(syncEvent) && isMusicTrack(attributes)) {
			MusicTrack track = SyncGenericConverter.toBean(attributes, MusicTrack.class);
			if (track != null) {
				libraryDao.saveOrUpdate(track);
			}
		}
	}

	private boolean isSaveEvent(SyncEventEntity syncEvent) {
		return SyncOperation.SAVE == syncEvent.getOperation();
	}

	private boolean isMusicTrack(Map<String, Object> attributes) {
		return CLIENT_TRACK_ENTITY_TYPE.equals(attributes.get(SyncGenericConverter.ENTITY));
	}

	private void updateDeltas(LibrarySyncStatus librarySyncStatus, Long snapshotId) {
		List<LibraryDelta> libraryDeltas = libraryDao.findDeltasBySnapshot(snapshotId, librarySyncStatus
				.getProcessedDelta());
		for (LibraryDelta libraryDelta : libraryDeltas) {
			SyncValueObject delta = libraryDelta.getDelta();
			List<String> events = delta.getEvents();
			for (String encodedEvents : events) {
				decodeAndProcessEvents(encodedEvents);
			}
		}
		librarySyncStatus.setProcessedDelta(librarySyncStatus.getCurrentDelta());
		libraryDao.update(librarySyncStatus);

	}

	private final class SyncTask implements Runnable {

		@Override
		public void run() {
			try {
				updateLibrariesSyncStatus();
			} catch (Exception e) {
				log.error("Unexpected exception synching library.", e);
			} finally {
				if (!syncExecutor.isShutdown()) {
					syncExecutor.schedule(this, SYNC_TASK_ACTIVE_DELAY, SYNC_TAKS_DELAY_UNIT);
				}
			}
		}
	}
}
