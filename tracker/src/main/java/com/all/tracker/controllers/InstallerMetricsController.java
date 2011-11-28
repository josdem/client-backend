package com.all.tracker.controllers;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.lang.reflect.InvocationTargetException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.all.tracker.model.InstallerMetrics;
import com.all.tracker.model.InstallerStatus;
import com.all.tracker.service.InternalIPService;

@Controller
@RequestMapping("/metrics/**")
public class InstallerMetricsController {

	@Autowired
	private HibernateTemplate ht;
	@Autowired
	private InternalIPService ipService;
	
	public static final String charset = "~";
	public static final String charset_inside = "=";

	private Log log = LogFactory.getLog(this.getClass());

	@RequestMapping(method = POST)
	@ResponseBody
	public String receiveMetricsByPost(@RequestBody String metrics, HttpServletRequest request) {
		try {
			saveMetrics(metrics, request.getRemoteAddr());
		} catch (InstantiationException e) {
			log.error(e, e);
		} catch (IllegalAccessException e) {
			log.error(e, e);
		}
		return "ok";
	}

	@RequestMapping(method = GET, value = "{metrics}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public String receiveMetricsByGet(@PathVariable String metrics, HttpServletRequest request) {
		try {
			saveMetrics(metrics, request.getRemoteAddr());
		} catch (InstantiationException e) {
			log.error(e, e);
		} catch (IllegalAccessException e) {
			log.error(e, e);
		}
		return "ok";
	}

	public <T> T setFeatures(String[] features, Class<T> clazz) throws InstantiationException, IllegalAccessException {
		if (clazz == null || features == null || features.length == 0) {
			log.error("exception when try to create a SyncAble entity");
			return null;
		}
		T obj = clazz.newInstance();
		for (String item : features) {
			String[] feature = splitMetrics(item, charset_inside);
			if (feature.length == 2) {
				if (PropertyUtils.isWriteable(obj, feature[0])) {
					try {
						PropertyUtils.setProperty(obj, feature[0], feature[1]);
					} catch (InvocationTargetException e) {
						log.error(e.getMessage());
					} catch (NoSuchMethodException e) {
						log.error(e.getMessage());
					}
				}
			}
		}
		return obj;
	}

	public String[] splitMetrics(String metrics, String charset) {
		return metrics.split(charset);
	}

	private void saveMetrics(String metrics, String ip) throws InstantiationException, IllegalAccessException {
		log.info("metric received from : " + ip + " data : " + metrics);
		if (ipService.isInternalIp(ip.trim())){
			log.error("is an internal ip, is gonna be ignored");
			return;
		}
		InstallerMetrics installerMetrics = setFeatures(splitMetrics(metrics, charset), InstallerMetrics.class);
		if ((installerMetrics.getOS() == null || installerMetrics.getOS().length() == 0)
				&& (installerMetrics.getJAVAVERSION() == null || installerMetrics.getJAVAVERSION().length() == 0)
				&& installerMetrics.getSTATUS() == null && installerMetrics.getMAC() == null) {
			log.error("Invalid stream : " + metrics);
			return;
		} else if (installerMetrics.getSTATUS().startsWith("CANCEL") || installerMetrics.getSTATUS().startsWith("FINISHED")) {
			InstallerStatus status = new InstallerStatus(installerMetrics.getSTATUS(), installerMetrics.getMAC());
			log.info(ToStringBuilder.reflectionToString(status));
			ht.save(status);
		} else {
			log.info(ToStringBuilder.reflectionToString(installerMetrics));
			ht.save(installerMetrics);
		}
	}
}
