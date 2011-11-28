package com.all.tracker.controllers;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONArray;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.all.testing.MockInyectRunner;
import com.all.testing.UnderTest;
import com.all.tracker.model.Notification;


@RunWith(MockInyectRunner.class)
public class TestNotificationsController {

	@UnderTest
	private NotificationsController controller;
	@Mock
	private HibernateTemplate ht;
	
	@Test
	public void shouldReturnJsonArrayWithNotifications() throws Exception {
		List<Notification> notifications = new ArrayList<Notification>();
		Notification notifA = new Notification("<html><b><i>All.com</i></b> has news for you", "description A", "http://www.all.com?someUrl=crazy#", new Date().getTime());
		notifA.setId(1L);
		notifications.add(notifA);
		Notification notifB = new Notification("title B", "description B", "link B", new Date().getTime());
		notifB.setId(2L);
		notifications.add(notifB);
		when(ht.find(anyString())).thenReturn(notifications );

		String jsonNotifications = controller.getNotifications();
		assertEquals(2, JSONArray.fromObject(jsonNotifications).size());
	}

	@Test
	public void shouldReturnEmptyJsonArrayIfNoneNotifications() throws Exception {
		List<Notification> notifications = new ArrayList<Notification>();
		when(ht.find(anyString())).thenReturn(notifications );

		String jsonNotifications = controller.getNotifications();
		assertEquals(0, JSONArray.fromObject(jsonNotifications).size());
	}

}
