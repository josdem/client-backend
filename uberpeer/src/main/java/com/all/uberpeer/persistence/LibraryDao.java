package com.all.uberpeer.persistence;

import static org.hibernate.criterion.Restrictions.and;
import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.gt;

import java.sql.SQLException;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;

import com.all.backend.commons.library.LibraryDelta;
import com.all.backend.commons.library.LibrarySnapshot;
import com.all.backend.commons.library.LibrarySyncStatus;

@Repository
public class LibraryDao {

	@Autowired
	private HibernateTemplate ht;

	@SuppressWarnings("unchecked")
	public List<LibrarySyncStatus> findUnprocessedLibraries() {
		return ht
				.find("From LibrarySyncStatus where processedSnapshot != currentSnapshot or processedDelta != currentDelta");
	}

	public <T> void update(T entity) {
		ht.update(entity);
	}

	public LibrarySnapshot findSnapshotByUserAndVersion(final String owner, final Integer version) {
		return ht.execute(new HibernateCallback<LibrarySnapshot>() {
			@Override
			public LibrarySnapshot doInHibernate(Session session) throws HibernateException, SQLException {
				Query query = session.createQuery("From LibrarySnapshot where email=:owner  and version=:version");
				query.setParameter("owner", owner);
				query.setParameter("version", version);
				return (LibrarySnapshot) query.uniqueResult();
			}
		});
	}

	public <T> void saveOrUpdate(T entity) {
		ht.saveOrUpdate(entity);
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

}
