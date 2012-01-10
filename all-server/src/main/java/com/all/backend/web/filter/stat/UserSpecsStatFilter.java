package com.all.backend.web.filter.stat;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.all.backend.web.filter.StatFilter;
import com.all.backend.web.persistence.AlertDao;
import com.all.backend.web.persistence.UserDao;
import com.all.shared.alert.AllNotificationAlert;
import com.all.shared.alert.AllVersionNotification;
import com.all.shared.model.ContactInfo;
import com.all.shared.stats.UserSpecs;

@Component
public class UserSpecsStatFilter implements StatFilter<UserSpecs> {
	private final static Log log = LogFactory.getLog(UserSpecsStatFilter.class);
	private final List<AllVersionNotification> broadcastNotifications = new LinkedList<AllVersionNotification>();
	private final Map<String, List<AllVersionNotification>> versionNotifications = new HashMap<String, List<AllVersionNotification>>();

	@Autowired
	private UserDao userDao;
	@Autowired
	private AlertDao alertsDao;

	@Override
	public Class<UserSpecs> getStatClass() {
		return UserSpecs.class;
	}

	@Override
	public void filter(UserSpecs userSpecs) {
		ContactInfo receiver = new ContactInfo(userDao.findUserByEmail(userSpecs.getEmail()));
		sendBroadcastNotifications(userSpecs, receiver);
		sendVersionNotifications(userSpecs, receiver);
	}

	@PostConstruct
	public void setup() {
		List<AllVersionNotification> notifications = alertsDao.loadVersionNotifications();
		versionNotifications.clear();
		broadcastNotifications.clear();
		for (AllVersionNotification notification : notifications) {
			loadNotification(notification);
		}
		log.info("Version Notifications loaded...");
	}

	private void sendVersionNotifications(UserSpecs userSpecs, ContactInfo receiver) {
		List<AllVersionNotification> notifications = versionNotifications.get(userSpecs.getVersion());
		if (notifications != null) {
			for (AllVersionNotification notification : notifications) {
				sendVersionNotification(receiver, notification);
			}
		}
	}

	private void sendBroadcastNotifications(UserSpecs userSpecs, ContactInfo receiver) {
		for (AllVersionNotification notification : broadcastNotifications) {
			if (notification.isExcludeVersion() && userSpecs.getVersion().equals(notification.getVersion())) {
				continue;
			}
			sendVersionNotification(receiver, notification);
		}
	}

	private void sendVersionNotification(ContactInfo receiver, AllVersionNotification notification) {
		Long timestamp = notification.getTimestamp();
		String header = notification.getHeader();
		String description = notification.getDescription();
		String link = notification.getLink();
		AllNotificationAlert alert = new AllNotificationAlert(receiver, timestamp, header, description, link);
		alertsDao.save(alert);
	}

	private void loadNotification(AllVersionNotification notification) {
		if (notification.isBroadcast()) {
			broadcastNotifications.add(notification);
		} else {
			List<AllVersionNotification> list = versionNotifications.get(notification.getVersion());
			if (list == null) {
				list = new LinkedList<AllVersionNotification>();
				versionNotifications.put(notification.getVersion(), list);
			}
			list.add(notification);
		}
	}

}
