package com.all.tracker.controllers;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.List;

import net.sf.json.JSONArray;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.all.tracker.model.Notification;

@Controller
@RequestMapping("/notifications/**")
@Deprecated
public class NotificationsController {

	@Autowired
	private HibernateTemplate ht;

	@SuppressWarnings("unchecked")
	@RequestMapping(method = GET)
	@ResponseBody
	public String getNotifications() {
		List<Notification> notifications = ht.find("From Notification");
		return JSONArray.fromObject(notifications).toString();
	}

}
