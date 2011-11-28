package com.all.tracker.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.all.tracker.model.InternalIPs;

@Service
public class InternalIPService {

	private static List<String> ips;
	@Autowired
	private HibernateTemplate ht;
	private Log log = LogFactory.getLog(this.getClass());

	public InternalIPService() {
		ips = new ArrayList<String>();
	}

	@SuppressWarnings("unchecked")
	@Scheduled(fixedDelay = 30000)
	public void updateListIP() {
		ips.clear();
		List<InternalIPs> internalIps = ht.find("from InternalIPs");
		log.info("reading All internal ips : " + internalIps.size());
		
		for (InternalIPs item : internalIps) {
			log.info("ip :" + item.getIp());
			ips.add(item.getIp());
		}
	}

	public boolean isInternalIp(String ip) {
		for (String item : ips) {
			if (item.compareTo(ip) == 0) {
				return true;
			}
		}
		return false;
	}
}
