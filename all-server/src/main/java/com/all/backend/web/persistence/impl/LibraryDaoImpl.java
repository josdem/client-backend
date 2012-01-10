package com.all.backend.web.persistence.impl;

import static org.hibernate.criterion.Restrictions.and;
import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.gt;

import java.sql.SQLException;
import java.util.List;

import javax.validation.Validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;

import com.all.backend.commons.library.LibraryDelta;
import com.all.backend.commons.library.LibrarySnapshot;
import com.all.backend.commons.library.LibrarySyncStatus;
import com.all.backend.web.persistence.LibraryDao;
import com.all.shared.model.SyncValueObject;

@Repository("libraryDao")
public class LibraryDaoImpl extends BaseDaoImpl implements LibraryDao {

	private final Log log = LogFactory.getLog(this.getClass());

	@Autowired
	public LibraryDaoImpl(HibernateTemplate ht, Validator validator, SimpleJdbcTemplate jdbcTemplate) {
		super(ht, validator, jdbcTemplate);
	}

	@SuppressWarnings("unchecked")
	public List<LibrarySnapshot> findSnapshotsByEmail(final String email) {
		return ht.executeFind(new HibernateCallback<List<LibrarySnapshot>>() {
			@Override
			public List<LibrarySnapshot> doInHibernate(Session session) throws HibernateException, SQLException {
				Criteria criteria = session.createCriteria(LibrarySnapshot.class);
				criteria.add(eq("email", email));
				return criteria.list();
			}
		});
	}

	@SuppressWarnings("unchecked")
	public List<LibraryDelta> findDeltasBySnapshot(final Long snapshotId, final int fromDelta) {
		return ht.executeFind(new HibernateCallback<List<LibraryDelta>>() {
			@Override
			public List<LibraryDelta> doInHibernate(Session session) throws HibernateException, SQLException {
				Criteria criteria = session.createCriteria(LibraryDelta.class);
				criteria.add(and(eq("snapshotId", snapshotId), gt("version", fromDelta)));
				return criteria.list();
			}
		});
	}

	public void saveSnapshot(LibrarySnapshot snapshot) {
		ht.save(snapshot);
		LibrarySyncStatus syncStatus = ht.get(LibrarySyncStatus.class, snapshot.getEmail());
		if (syncStatus == null) {
			syncStatus = new LibrarySyncStatus(snapshot.getEmail(), snapshot.getVersion(), 0);
		} else {
			// TODO HOW CAN THIS HAPPEN
			syncStatus.setCurrentSnapshot(snapshot.getVersion());
		}
		ht.saveOrUpdate(syncStatus);
	}

	public void updateSnapshot(final LibrarySnapshot oldSnapshot, final LibrarySnapshot newSnapshot) {
		ht.execute(new HibernateCallback<Void>() {
			@Override
			public Void doInHibernate(Session session) throws HibernateException, SQLException {
				session.save(newSnapshot);
				session.delete(oldSnapshot);
				Query query = session.createQuery("Delete from LibraryDelta ld where ld.snapshotId=:snapshotId");
				query.setParameter("snapshotId", oldSnapshot.getId());
				query.executeUpdate();
				Query statusQuery = session.createQuery("From LibrarySyncStatus where owner=:owner");
				LibrarySyncStatus syncStatus = (LibrarySyncStatus) statusQuery.uniqueResult();
				statusQuery.setParameter("owner", newSnapshot.getEmail());
				syncStatus.setCurrentSnapshot(newSnapshot.getVersion());
				syncStatus.setCurrentDelta(0);
				session.update(syncStatus);
				return null;
			}
		});
	}

	public LibraryDelta saveDelta(final Long snapshotId, final SyncValueObject deltaObject) {
		return ht.execute(new HibernateCallback<LibraryDelta>() {
			@Override
			public LibraryDelta doInHibernate(Session session) throws HibernateException, SQLException {
				Query query = session.createQuery("Select count(*) from LibraryDelta ld where ld.snapshotId=:snapshotId");
				query.setParameter("snapshotId", snapshotId);
				Long count = (Long) query.uniqueResult();
				deltaObject.setDelta(count.intValue() + 1);
				LibraryDelta delta = new LibraryDelta(snapshotId, deltaObject);
				session.save(delta);
				Query statusQuery = session.createQuery("From LibrarySyncStatus where owner=:owner");
				statusQuery.setParameter("owner", deltaObject.getEmail());
				LibrarySyncStatus syncStatus = (LibrarySyncStatus) statusQuery.uniqueResult();
				if (syncStatus == null) {
					log.warn("Could not find a previous sync stats when trying to commit a delta for " + deltaObject.getEmail());
					syncStatus = new LibrarySyncStatus(deltaObject.getEmail(), deltaObject.getSnapshot(), deltaObject.getDelta());
				} else {
					syncStatus.setCurrentDelta(deltaObject.getDelta());
				}
				session.saveOrUpdate(syncStatus);
				return delta;
			}
		});
	}

	@SuppressWarnings("unchecked")
	public List<LibrarySnapshot> findSnapshotsByEmailAndVersion(final String email, final int snapshot) {
		return ht.executeFind(new HibernateCallback<List<LibrarySnapshot>>() {
			@Override
			public List<LibrarySnapshot> doInHibernate(Session session) throws HibernateException, SQLException {
				Criteria criteria = session.createCriteria(LibrarySnapshot.class);
				criteria.add(eq("email", email));
				criteria.add(eq("version", snapshot));
				return criteria.list();
			}
		});
	}

}
