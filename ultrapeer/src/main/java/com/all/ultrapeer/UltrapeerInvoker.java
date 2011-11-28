package com.all.ultrapeer;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.all.dht.DhtManager;
import com.all.services.ServiceInvoker;
import com.all.ultrapeer.services.DefaultContactsService;
import com.all.ultrapeer.services.PresenceService;

@Component
@SuppressWarnings("deprecation")
// com.all.ultrapeer.services.DefaultContactsService has to die.
public class UltrapeerInvoker implements ServiceInvoker {

	private static final String ARGS_SEPARATOR = ".";

	private final Log log = LogFactory.getLog(UltrapeerInvoker.class);

	@Autowired
	private DhtManager dhtManager;
	@Autowired
	private PresenceService presenceService;
	@Autowired
	private DefaultContactsService defaultContactsService;

	@Override
	public Object invoke(String action) {
		String methodName = null;
		String argsStr = "";
		if (action.contains(ARGS_SEPARATOR)) {
			methodName = action.substring(0, action.indexOf(ARGS_SEPARATOR));
			argsStr = action.substring(action.indexOf(ARGS_SEPARATOR) + 1, action.length());
		} else {
			methodName = action;
		}
		Object result = null;
		try {
			if (argsStr.isEmpty()) {
				Method method = getClass().getDeclaredMethod(methodName);
				result = method.invoke(this);
			} else {
				Object args = argsStr.contains(ARGS_SEPARATOR) ? argsStr.split(ARGS_SEPARATOR) : argsStr;
				Method method = getClass().getDeclaredMethod(methodName, args.getClass());
				result = method.invoke(this, args);
			}
		} catch (Exception e) {
			log.error(e, e);
			result = e.toString();
		}
		return result != null ? result.toString() : null;
	}

	public String keys() {
		Set<String> storedKeys = dhtManager.getAllKeys();
		if (storedKeys != null && !storedKeys.isEmpty()) {
			StringBuilder sb = new StringBuilder("\n");
			for (String key : storedKeys) {
				sb.append(key).append("\n");
			}
			return sb.toString();
		} else {
			return null;
		}
	}

	public String localKeys() {
		StringBuilder sb = new StringBuilder("\n");
		Map<String, String> localKeys = dhtManager.getLocalKeys();
		if (localKeys != null && !localKeys.isEmpty()) {
			for (String key : localKeys.keySet()) {
				sb.append(key).append("\t").append(localKeys.get(key)).append("\n");
			}
		}
		return sb.toString();
	}

	public String nodeId() {
		return dhtManager.getNodeId();
	}

	public String more(String primaryKey) {
		return dhtManager.more(primaryKey);
	}

	public String presenceInfo() {
		return presenceService.getInfo();
	}

	public String defaultContacts() {
		return defaultContactsService.getInfo();
	}

	public String reloadDCL() {
		defaultContactsService.start();
		return defaultContactsService.getInfo();
	}

	public String gc() {
		log.info("Will invoke GarbageCollector...");
		StringBuilder sb = new StringBuilder();
		sb.append("\nTotal Memory: ");
		sb.append(Runtime.getRuntime().totalMemory() / 1024);
		sb.append("\nFree Memory Before: ");
		sb.append(Runtime.getRuntime().freeMemory() / 1024);
		System.gc();
		sb.append("\nFree Memory After: ");
		sb.append(Runtime.getRuntime().freeMemory() / 1024);
		return sb.toString();
	}

}
