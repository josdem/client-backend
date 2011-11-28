package com.all.rds.service.impl;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.all.rds.model.CachedTrack;
import com.all.rds.model.Category;
import com.all.rds.model.TopPlaylist;
import com.all.rds.service.TopHundredService;

@Service
public class TopHundredServiceImpl implements TopHundredService {

	@Autowired
	private HibernateTemplate hibernateTemplate;

	public List<Category> getCategories() {
		return hibernateTemplate.loadAll(Category.class);
	}

	@SuppressWarnings("unchecked")
	public List<TopPlaylist> getTopPlaylists(final Long categoryId) {
		return hibernateTemplate.executeFind(new HibernateCallback<List<TopPlaylist>>() {
			@Override
			public List<TopPlaylist> doInHibernate(Session session) throws HibernateException, SQLException {
				Query query = session.createQuery("From Category where id=:categoryId");
				query.setParameter("categoryId", categoryId);
				Category category = (Category) query.uniqueResult();
				return (List<TopPlaylist>) (category != null ? category.list() : Collections.emptyList());
			}
		});
	}

	@SuppressWarnings("unchecked")
	public List<CachedTrack> getTracks(final String topPlaylistId) {
		return hibernateTemplate.executeFind(new HibernateCallback<List<CachedTrack>>() {
			@Override
			public List<CachedTrack> doInHibernate(Session session) throws HibernateException, SQLException {
				Query query = session
						.createQuery("Select ct From CachedTrack ct, TopPlaylistTrack tpt where tpt.playlist=:topPlaylistId and tpt.track=ct.hashcode order by tpt.numTrack");
				query.setParameter("topPlaylistId", topPlaylistId);
				return query.list();
			}
		});
	}

	@Override
	public TopPlaylist getRandomTopPlaylist(){
		return hibernateTemplate.execute(new HibernateCallback<TopPlaylist>() {
			@Override
			public TopPlaylist doInHibernate(Session session) throws HibernateException, SQLException {
				Query query = session.createQuery("From TopPlaylist order by rand()");
				query.setMaxResults(1);
				TopPlaylist topPlaylist = (TopPlaylist) query.uniqueResult();
				query = session
						.createQuery("Select ct From CachedTrack ct, TopPlaylistTrack tpt where tpt.playlist=:topPlaylistId and tpt.track=ct.hashcode order by tpt.numTrack");
				query.setParameter("topPlaylistId", topPlaylist.getHashcode());
				@SuppressWarnings("unchecked")
				List<CachedTrack> tracks = query.list();
				topPlaylist.setTracks(tracks);
				return topPlaylist;
			}
		});
	}
	
}
