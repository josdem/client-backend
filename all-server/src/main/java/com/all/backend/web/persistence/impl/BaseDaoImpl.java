package com.all.backend.web.persistence.impl;

import static org.hibernate.criterion.Restrictions.in;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.persistence.Id;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.test.jdbc.SimpleJdbcTestUtils;

import com.all.backend.commons.signup.PasswordResetRequest;
import com.all.backend.web.exception.DomainValidationException;
import com.all.backend.web.persistence.BaseDao;
import com.all.shared.model.City;

@Repository("baseDao")
public class BaseDaoImpl implements BaseDao {
	protected HibernateTemplate ht;
	private Validator validator;
	private SimpleJdbcTemplate jdbcTemplate;

	@Autowired
	public BaseDaoImpl(HibernateTemplate ht, Validator validator, SimpleJdbcTemplate jdbcTemplate) {
		this.ht = ht;
		this.validator = validator;
		this.jdbcTemplate = jdbcTemplate;
	}

	@SuppressWarnings("unchecked")
	public long count(final Class<?> objectClass) {

		return (Long) ht.execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query q = session.createQuery("select count(*) from " + objectClass.getSimpleName());
				return q.uniqueResult();
			}
		});
	}

	@PostConstruct
	public void setup() {
		if (count(City.class) == 0) {
			Resource resource = new ClassPathResource("/scripts/cities_inserts.sql");
			SimpleJdbcTestUtils.executeSqlScript(jdbcTemplate, resource, false);
		}

		if (count(PasswordResetRequest.class) == 0) {
			PasswordResetRequest p = new PasswordResetRequest();
			GregorianCalendar gc = new GregorianCalendar();
			gc.set(2010, 1, 1);
			p.setResetRequestExpireDate(gc.getTime());// TODO: do not use deprecated constructors
			p.setUserId(1l);
			p.setPasswordRequestKey("XX");
			this.save(p);
		}

	}

	public Serializable save(Object value) throws DomainValidationException {
		validateDomainObject(value);
		return ht.save(value);
	}

	private void validateDomainObject(Object value) throws DomainValidationException {
		Set<ConstraintViolation<Object>> violations = validator.validate(value);
		if (violations.size() > 0) {
			throw new DomainValidationException(violations);
		}
	}

	public void update(Object value) throws DomainValidationException {
		validateDomainObject(value);
		ht.update(value);
	}

	public void delete(Object value) {
		ht.delete(value);
	}

	public void merge(Object value) {
		ht.merge(value);
	}

	@SuppressWarnings("unchecked")
	public List find(String hql) {
		return ht.find(hql);
	}

	public <T> List<T> findAll(Class<T> clazz) {
		return ht.loadAll(clazz);
	}

	@SuppressWarnings("unchecked")
	public void delete(Class clazz, Serializable id) {
		ht.delete(find(clazz, id));
	}

	@SuppressWarnings("unchecked")
	public void deleteAll(Collection entities) {
		ht.deleteAll(entities);
	}

	public <T> T find(Class<T> clazz, Serializable id) {
		return ht.get(clazz, id);
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> find(final Class<T> clazz, final Set<? extends Serializable> ids) {
		if (ids.isEmpty()) {
			return Collections.EMPTY_LIST;
		}
		final String idFieldName = findIdFieldName(clazz);

		if (idFieldName == null) {
			throw new NullPointerException("The class " + clazz + " does not declare an @Id field");
		}

		List<T> results = ht.executeFind(new HibernateCallback<List<T>>() {
			@Override
			public List<T> doInHibernate(Session session) throws HibernateException, SQLException {
				Criteria criteria = session.createCriteria(clazz);
				criteria.add(in(idFieldName, ids));
				return criteria.list();
			}
		});

		return results;
	}

	@SuppressWarnings("unchecked")
	private String findIdFieldName(final Class clazz) {
		String fieldName = null;
		for (Field f : clazz.getDeclaredFields()) {
			if (f.isAnnotationPresent(Id.class)) {
				fieldName = f.getName();
				break;
			}
		}
		return fieldName;
	}

	@Override
	public void saveAll(final List<?> values) {
		ht.execute(new HibernateCallback<Void>() {
			@Override
			public Void doInHibernate(Session session) throws HibernateException, SQLException {
				for (Object value : values) {
					session.save(value);
				}
				return null;
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deleteAll(final Class clazz, final Set<? extends Serializable> ids) {
		ht.deleteAll(find(clazz, ids));
	}

	@Override
	public void saveOrUpdate(Object entity) {
		ht.saveOrUpdate(entity);
	}

}
