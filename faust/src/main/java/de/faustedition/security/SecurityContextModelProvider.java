package de.faustedition.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

public class SecurityContextModelProvider implements WebRequestInterceptor {

	@Override
	public void afterCompletion(WebRequest request, Exception ex) throws Exception {
	}

	@Override
	public void postHandle(WebRequest request, ModelMap model) throws Exception {
		if (model == null) {
			return;
		}
		
		String authName = "";
		List<String> authorities = new ArrayList<String>();

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			if (auth.getName() != null) {
				authName = auth.getName();
			}
			for (GrantedAuthority granted : auth.getAuthorities()) {
				authorities.add(granted.getAuthority());
			}
		}

		model.put("authName", authName);
		model.put("authAuthorities", Collections.unmodifiableList(authorities));
	}

	@Override
	public void preHandle(WebRequest request) throws Exception {
	}

}
