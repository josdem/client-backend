package com.all.backend.web.persistence.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;

import com.all.backend.web.persistence.StatsDao;
import com.all.shared.stats.AllStat;

@Repository
public class StatsDaoHibernateImpl implements StatsDao {

	@Autowired
	private HibernateTemplate ht;

	public void saveOrUpdate(AllStat stat) {
		ht.saveOrUpdate(stat);
	}
}
