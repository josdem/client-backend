package com.all.uberpeer.persistence;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;

import com.all.shared.model.ContactInfo;
import com.all.shared.model.User;

@Repository
public class ContactDao {

	@Autowired
	private HibernateTemplate ht;

	public ContactInfo findContactByEmail(final String email) {
		return ht.execute(new HibernateCallback<ContactInfo>() {
			@Override
			public ContactInfo doInHibernate(Session session) throws HibernateException, SQLException {
				Query query = session.createQuery("from User u where u.email=:email");
				query.setString("email", email);
				User user = (User) query.uniqueResult();
				if (user != null) {
					return new ContactInfo(user);
				}
				return null;
			}
		});

	}
}
