package com.all.backend.web.persistence.impl;

import static org.hibernate.criterion.Restrictions.and;
import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.in;
import static org.hibernate.criterion.Restrictions.or;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.validation.Validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;

import com.all.backend.commons.contact.ContactsRelationship;
import com.all.backend.commons.contact.DefaultContacts;
import com.all.backend.commons.contact.FriendshipStatus;
import com.all.backend.commons.signup.PasswordResetRequest;
import com.all.backend.commons.signup.RegistrationPending;
import com.all.backend.web.persistence.UserDao;
import com.all.shared.model.ContactRequest;
import com.all.shared.model.PendingEmail;
import com.all.shared.model.User;

@Repository("userDao")
public class UserDaoImpl extends BaseDaoImpl implements UserDao {

	private Log log = LogFactory.getLog(this.getClass());

	@Autowired
	public UserDaoImpl(HibernateTemplate ht, Validator validator, SimpleJdbcTemplate jdbcTemplate) {
		super(ht, validator, jdbcTemplate);
	}

	public User findUserByEmail(final String email) {
		return ht.execute(new HibernateCallback<User>() {
			@Override
			public User doInHibernate(Session session) throws HibernateException, SQLException {
				Query query = session.createQuery("from User u where u.email=:email");
				query.setString("email", email);
				return (User) query.uniqueResult();
			}
		});
	}

	@SuppressWarnings("unchecked")
	public List<User> findUsersByName(final String searchName, final int index) {
		return ht.executeFind(new HibernateCallback<List<User>>() {
			@Override
			public List<User> doInHibernate(Session session) throws HibernateException, SQLException {
				Criteria criteria = session.createCriteria(User.class);
				criteria.add(Restrictions.or(Restrictions.ilike("searchName", searchName.replaceAll(" ", "%") + "%"),
						Restrictions.ilike("nickName", "%" + searchName + "%")));
				criteria.setFirstResult(index);
				// criteria.setMaxResults(BackendConstants.MAX_RESULTS);
				return criteria.list();
			}
		});
	}

	public Long countUsersByName(final String searchName) {
		return ht.execute(new HibernateCallback<Long>() {
			@Override
			public Long doInHibernate(Session session) throws HibernateException, SQLException {
				String hql = "SELECT count(*) FROM User c WHERE c.searchName like '"
						+ (searchName.replaceAll(" ", "%") + "%") + "'";
				Query query = session.createQuery(hql);
				return (Long) query.uniqueResult();
			}
		});
	}

	public RegistrationPending findRegistrationByUserId(final Long userId) {
		return ht.execute(new HibernateCallback<RegistrationPending>() {
			@Override
			public RegistrationPending doInHibernate(Session session) throws HibernateException, SQLException {
				Query query = session.createQuery("from RegistrationPending rp where rp.userId=:userId");
				query.setLong("userId", userId);
				return (RegistrationPending) query.uniqueResult();
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PendingEmail> getPendingEmails(Long userId) {
		PendingEmail exampleEntity = new PendingEmail(userId);
		List<PendingEmail> pendingEmails = ht.findByExample(exampleEntity);
		return pendingEmails;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deleteContacts(final Long userId, final Set<Long> contactsIds) {
		List<ContactsRelationship> contactRelationshipsToDelete = ht
				.executeFind(new HibernateCallback<List<ContactsRelationship>>() {
					@Override
					public List<ContactsRelationship> doInHibernate(Session session) throws HibernateException,
							SQLException {
						Criteria criteria = session.createCriteria(ContactsRelationship.class);
						criteria.add(eq("idUser", userId));
						criteria.add(in("idFriend", contactsIds));
						return criteria.list();
					}
				});
		blockContacts(contactRelationshipsToDelete);

	}

	private void blockContacts(List<ContactsRelationship> contactRelationshipsToDelete) {
		for (ContactsRelationship relationshipToDelete : contactRelationshipsToDelete) {
			relationshipToDelete.setStatus(FriendshipStatus.BLOCKED);
			saveOrUpdate(relationshipToDelete);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ContactRequest findContactRequest(final Long idRequester, final Long idRequested) {
		List<ContactRequest> contactRequests = ht.executeFind(new HibernateCallback<List<ContactRequest>>() {
			@Override
			public List<ContactRequest> doInHibernate(Session session) throws HibernateException, SQLException {
				Criteria criteria = session.createCriteria(ContactRequest.class);
				criteria.add(or(and(eq("idRequester", idRequester), eq("idRequested", idRequested)),
						and(eq("idRequester", idRequested), eq("idRequested", idRequester))));
				return criteria.list();
			}
		});
		return contactRequests.isEmpty() ? null : contactRequests.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ContactRequest> findPendingRequests(final Long idRequested) {
		List<ContactRequest> contactRequests = ht.executeFind(new HibernateCallback<List<ContactRequest>>() {
			@Override
			public List<ContactRequest> doInHibernate(Session session) throws HibernateException, SQLException {
				Criteria criteria = session.createCriteria(ContactRequest.class);
				criteria.add(and(eq("idRequested", idRequested), eq("accepted", false)));
				return criteria.list();
			}
		});
		return contactRequests;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ContactsRelationship findContact(final Long userId, final Long friendId) {
		List<ContactsRelationship> contactRelationships = ht
				.executeFind(new HibernateCallback<List<ContactsRelationship>>() {
					@Override
					public List<ContactsRelationship> doInHibernate(Session session) throws HibernateException,
							SQLException {
						Criteria criteria = session.createCriteria(ContactsRelationship.class);
						criteria.add(and(eq("idUser", userId), eq("idFriend", friendId)));
						return criteria.list();
					}
				});

		return contactRelationships.isEmpty() ? null : contactRelationships.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PendingEmail> findPendingEmails(String requestedEmail) {
		PendingEmail pendingEmail = new PendingEmail();
		pendingEmail.setToMail(requestedEmail);
		return ht.findByExample(pendingEmail);
	}

	@SuppressWarnings("unchecked")
	@Override
	public PendingEmail findPendingEmail(Long idRequester, String email) {
		PendingEmail pendingEmail = new PendingEmail(idRequester, email);
		List<PendingEmail> pendingEmails = ht.findByExample(pendingEmail);
		return pendingEmails.isEmpty() ? null : pendingEmails.get(0);
	}

	@SuppressWarnings("unchecked")
	public PasswordResetRequest findResetPasswordRequestByKey(final String key) {
		List<PasswordResetRequest> results = ht.executeFind(new HibernateCallback<List<PasswordResetRequest>>() {
			@Override
			public List<PasswordResetRequest> doInHibernate(Session session) throws HibernateException, SQLException {
				Criteria criteria = session.createCriteria(PasswordResetRequest.class);
				criteria.add(eq("passwordRequestKey", key));
				return criteria.list();
			}
		});

		return results.isEmpty() ? null : results.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> findUsersByEmail(final List<String> emailsList) {
		List<User> contacts = Collections.EMPTY_LIST;
		if (!emailsList.isEmpty()) {
			contacts = ht.executeFind(new HibernateCallback<List<User>>() {
				@Override
				public List<User> doInHibernate(Session session) throws HibernateException, SQLException {
					Criteria criteria = session.createCriteria(User.class);
					criteria.add(in("email", emailsList.toArray()));
					return criteria.list();
				}
			});

		}
		return contacts;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> findUsersNotAddedToMyContactsFromUserList(final List<Long> registeredUsersList, final Long userId) {
		List<Long> myContactsIds = new ArrayList<Long>();
		myContactsIds = (List<Long>) ht.execute(new HibernateCallback<List<Long>>() {
			@Override
			public List<Long> doInHibernate(Session session) throws HibernateException, SQLException {
				Query query = session
						.createQuery("select ctc.idFriend from Contact ctc where ctc.idUser=:idUser and ctc.idFriend in(:idFriend)");
				query.setLong("idUser", userId);
				query.setParameterList("idFriend", registeredUsersList);
				return (List<Long>) query.list();
			}
		});
		if (myContactsIds != null && !myContactsIds.isEmpty()) {
			registeredUsersList.removeAll(myContactsIds);
		}
		return findMyContactsFromContactInfo(registeredUsersList);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> findMyContactsFromContactInfo(final List<Long> contactInfoIds) {
		List<User> result = ht.executeFind(new HibernateCallback<List<User>>() {
			@Override
			public List<User> doInHibernate(Session session) throws HibernateException, SQLException {
				Criteria criteria = session.createCriteria(User.class);
				criteria.add(in("id", contactInfoIds));
				return criteria.list();
			}
		});
		log.debug("Returned Users size" + result.size());
		return result;
	}

	@Override
	public List<User> getDefaultContacts() {
		List<DefaultContacts> defaultContactsList = findAll(DefaultContacts.class);
		List<User> defaultContacts = new ArrayList<User>();
		User user;
		for (DefaultContacts contact : defaultContactsList) {
			user = findUserByEmail(contact.getEmail());
			if (user != null) {
				defaultContacts.add(user);
			}
		}
		return defaultContacts;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ContactsRelationship> getContactsRelationship(Long userId) {
		ContactsRelationship exampleEntity = new ContactsRelationship(userId);
		List<ContactsRelationship> contacts = ht.findByExample(exampleEntity);
		return contacts;
	}

	@Override
	public <T> List<T> find(Class<T> clazz, Set<? extends Serializable> ids) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findContactsRelationshipFromUserId(final Long userId) {
		List<Long> result = ht.executeFind(new HibernateCallback<List<Long>>() {
			@Override
			public List<Long> doInHibernate(Session session) throws HibernateException, SQLException {
				Query query = session.createQuery("select crs.idUser from ContactsRelationship crs where crs.idFriend=:userId");
				query.setLong("userId", userId);
				return (List<Long>) query.list();
			}
		});
		log.debug("Returned UserRelationships EmailList size" + result.size());
		return result;
	}

}
