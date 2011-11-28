package com.all.uberpeer.persistence;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;

import com.all.backend.commons.media.MusicTrack;

@Repository
public class TrackDao {

	@Autowired
	private HibernateTemplate ht;

	public MusicTrack findTrackByHashcode(final String hashcode) {
		return ht.execute(new HibernateCallback<MusicTrack>() {
			@Override
			public MusicTrack doInHibernate(Session session) throws HibernateException, SQLException {
				Query query = session.createQuery("from MUSIC_TRACKS u where u.hashcode=:hashcode");
				query.setString("hashcode", hashcode);
				MusicTrack track = (MusicTrack) query.uniqueResult();
				return track;
			}
		});

	}
}
