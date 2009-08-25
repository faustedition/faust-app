package de.faustedition.web;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

public class FaustPathUtils {
	public static String getPath(HttpServletRequest request) {
		return StringUtils.strip(StringUtils.defaultString(request.getPathInfo()), "/");
	}
}
