package de.faustedition.web;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

public class ControllerUtil {

	public static String getBaseURI(HttpServletRequest req) {
		String scheme = req.getScheme();
		String port = Integer.toString(req.getServerPort());
		if (("http".equals(scheme) && "80".equals(port)) || ("https".equals(scheme) && "443".equals(port))) {
			port = null;
		}
		return (scheme + "://" + req.getServerName() + (port == null ? "" : ":" + port) + req.getContextPath() + "/");
	}

	public static String getPath(HttpServletRequest request, String prefix) {
		String path = StringUtils.strip(StringUtils.defaultString(request.getPathInfo()), "/");
		path = StringUtils.removeStart(path, StringUtils.strip(StringUtils.defaultString(prefix), "/"));
		return StringUtils.strip(FilenameUtils.normalize(path), "/");
	}
}
