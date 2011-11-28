package com.all.uberpeer.services.persistence;

import static org.hibernate.criterion.Restrictions.and;
import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.gt;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.all.backend.commons.library.LibraryDelta;
import com.all.backend.commons.library.LibrarySnapshot;
import com.all.uberpeer.persistence.LibraryDao;


public class TestLibraryDao {

	@InjectMocks
	private LibraryDao dao = new LibraryDao();
	
	@Mock
	private HibernateTemplate ht;
	
	@Captor
	private ArgumentCaptor<HibernateCallback<LibrarySnapshot>> captor;

	@Mock
	private Session session;

	@Mock
	private Query query;
	
	@Mock
	private Criteria criteria;
	
	@Captor
	private ArgumentCaptor<Criterion> criterionCaptor;
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void shouldFindUnprocessedLibraries() throws Exception {
		dao.findUnprocessedLibraries();
		verify(ht).find(anyString());
	}
	
	@Test
	public void shouldFindCurrentSnapshotForUser() throws Exception {
		String owner = "user@all.com";
		Integer version = 1;
		dao.findSnapshotByUserAndVersion(owner, version);
		verify(ht).execute(captor.capture());
		HibernateCallback<LibrarySnapshot> callback = captor.getValue();
		when(session.createQuery(anyString())).thenReturn(query);
		callback.doInHibernate(session);
		verify(query).setParameter("owner", owner);
		verify(query).setParameter("version", version);
		verify(query).uniqueResult();
	}
	
	@Test
	public void shouldFindDeltasBySnapshot() throws Exception {
		int fromDelta = 3;
		Long snapshotId = 100L;
		dao.findDeltasBySnapshot(snapshotId, fromDelta);

		verify(ht).executeFind(captor.capture());

		HibernateCallback<?> callback = captor.getValue();
		when(session.createCriteria(LibraryDelta.class)).thenReturn(criteria);
		Criterion expectedCriterion = and(eq("snapshotId", snapshotId), gt("version", fromDelta));

		callback.doInHibernate(session);

		verify(criteria).add(criterionCaptor.capture());
		Criterion criterion = criterionCaptor.getValue();
		assertEquals(expectedCriterion.toString(), criterion.toString());
		verify(criteria).list();
	}
	
	
	@Test
	public void coverage() throws Exception {
		dao.saveOrUpdate(anyObject());
		verify(ht).saveOrUpdate(anyObject());
		dao.update(anyObject());
		verify(ht).update(anyObject());
		
	}
	
}
