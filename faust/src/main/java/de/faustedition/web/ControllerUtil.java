package de.faustedition.web;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import de.faustedition.model.ObjectNotFoundException;

public class ControllerUtil {

	public static String getBaseURI(HttpServletRequest req) {
		String scheme = req.getScheme();
		String port = Integer.toString(req.getServerPort());
		if (("http".equals(scheme) && "80".equals(port)) || ("https".equals(scheme) && "443".equals(port))) {
			port = null;
		}		
		return (scheme + "://" + req.getServerName() + (port == null ? "" : ":" + port) + req.getContextPath() + "/");	
	}

	public static String getPath(HttpServletRequest request) {
		return FilenameUtils.normalize(StringUtils.strip(StringUtils.defaultString(request.getPathInfo()), "/"));
	}

	public static Deque<String> getPathComponents(HttpServletRequest request) {
		return getPathComponents(getPath(request));
	}

	public static Deque<String> getPathComponents(String path) {
		return new ArrayDeque<String>(Arrays.asList(StringUtils.split(path, "/")));

	}

	public static <T> T foundObject(T object) throws ObjectNotFoundException {
		if (object == null) {
			throw new ObjectNotFoundException();
		}
		return object;
	}
}
