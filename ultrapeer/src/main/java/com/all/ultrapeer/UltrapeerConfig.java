package com.all.ultrapeer;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.all.peer.commons.util.PeerSettings;

@Service
public class UltrapeerConfig extends PeerSettings {

	private final Log log = LogFactory.getLog(this.getClass());
	@Autowired
	private Properties dhtConfig;
	@Autowired
	private Properties ultrapeerSettings;

	private final Map<String, Properties> config = new HashMap<String, Properties>();

	@PostConstruct
	public void initialize() {
		fillConfigMap(dhtConfig);
		fillConfigMap(ultrapeerSettings);
	}

	private void fillConfigMap(Properties properties) {
		@SuppressWarnings("unchecked")
		Enumeration<String> propertyNames = (Enumeration<String>) properties.propertyNames();
		while (propertyNames.hasMoreElements()) {
			String property = propertyNames.nextElement();
			if (config.containsKey(property)) {
				throw new IllegalStateException("Duplicated property name.");
			}
			config.put(property, properties);
		}
	}

	@Override
	public String getName() {
		return "ALL-ULTRAPEER[" + getPublicIp() + "]";
	}

	public String getProperty(String property) {
		return config.get(property).getProperty(property);
	}

	public void setProperty(String property, String value) {
		config.get(property).setProperty(property, value);
		log.info("Property " + property + " = " + getProperty(property));
	}

	public <T> T getTypedProperty(String property, Class<T> clazz) {
		try {
			return clazz.getConstructor(String.class).newInstance(getProperty(property));
		} catch (Exception e) {
			return null;
		}
	}

	public String getUrl(String serverKey, String actionKey) {
		validateNotNull(serverKey);
		validateNotNull(actionKey);
		return getProperty(serverKey) + getProperty(actionKey);
	}

	private void validateNotNull(String key) {
		if (key == null) {
			throw new IllegalArgumentException("Parameter is null");
		}
	}

}
