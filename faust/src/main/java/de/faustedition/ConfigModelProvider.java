package de.faustedition;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

public class ConfigModelProvider implements WebRequestInterceptor, InitializingBean {

	@Autowired
	@Qualifier("config")
	private Properties config;

	private Map<String, String> configMap;

	@Override
	public void afterCompletion(WebRequest request, Exception ex) throws Exception {
	}

	@Override
	public void postHandle(WebRequest request, ModelMap model) throws Exception {
		if (model != null) {
			model.addAttribute("config", configMap);
		}
	}

	@Override
	public void preHandle(WebRequest request) throws Exception {
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		configMap = new HashMap<String, String>();
		for (Object key : config.keySet()) {
			configMap.put(key.toString(), config.get(key).toString());
		}
		configMap = Collections.unmodifiableMap(configMap);
	}

}
