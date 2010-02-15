package de.faustedition.web;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

public class HasRoleTemplateMethod implements TemplateMethodModel {

	@SuppressWarnings("unchecked")
	@Override
	public Object exec(List arguments) throws TemplateModelException {
		if (arguments.size() != 1) {
			throw new TemplateModelException("Need a role name as single argument");
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			Collection<GrantedAuthority> authorities = authentication.getAuthorities();
			if (authorities != null) {
				for (GrantedAuthority authority : authorities) {
					if (authority.getAuthority().equals(arguments.get(0))) {
						return Boolean.TRUE;
					}
				}
			}
		}
		return Boolean.FALSE;
	}
}
