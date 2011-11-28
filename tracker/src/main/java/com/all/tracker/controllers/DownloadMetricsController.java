package com.all.tracker.controllers;


import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.lang.reflect.InvocationTargetException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.all.tracker.model.DownloaderMetrics;
import com.all.tracker.service.InternalIPService;

@Controller
@RequestMapping("/download_metrics/**")
public class DownloadMetricsController {

	@Autowired
	private HibernateTemplate ht;
	public static final String charset = "~";
	public static final String charset_inside = "=";
	@Autowired
	private InternalIPService ipService;
	private Log log = LogFactory.getLog(this.getClass());

	@RequestMapping(method = PUT)
	@ResponseBody
	public String receiveMetricsByPut(@RequestBody String metrics, HttpServletRequest request) {
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
		DownloaderMetrics downloaderMetrics = setFeatures(splitMetrics(metrics, charset), DownloaderMetrics.class);
		if (downloaderMetrics.getBYTES() == null && downloaderMetrics.getCODE() == null && downloaderMetrics.getDATEREGISTERED() == null &&
				downloaderMetrics.getDOWNLOADVERSION() == null && downloaderMetrics.getEXPLORER() == null){
			log.error("METRIC DOES NOT MATCH REQUIREMENTS : " + metrics);
		}
		log.info(ToStringBuilder.reflectionToString(downloaderMetrics));
		ht.save(downloaderMetrics);
	}

}
