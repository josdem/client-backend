package com.all.backend.web.persistence.impl;

import java.sql.SQLException;
import java.util.List;

import javax.validation.Validator;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;

import com.all.backend.commons.alert.AlertEntity;
import com.all.backend.web.persistence.AlertDao;
import com.all.shared.alert.AllVersionNotification;

@Repository("alertDao")
public class AlertDaoImpl extends BaseDaoImpl implements AlertDao {

	@Autowired
	public AlertDaoImpl(HibernateTemplate ht, Validator validator, SimpleJdbcTemplate jdbcTemplate) {
		super(ht, validator, jdbcTemplate);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<AlertEntity> findAlertsForUser(final String userId) {
		return ht.executeFind(new HibernateCallback<List<AlertEntity>>() {
			@Override
			public List<AlertEntity> doInHibernate(Session session) throws HibernateException, SQLException {
				return session.createCriteria(AlertEntity.class).add(Restrictions.eq("receiver", userId)).list();
			}
		});
	}

	@Override
	public List<AllVersionNotification> loadVersionNotifications() {
		return ht.loadAll(AllVersionNotification.class);
	}

}
