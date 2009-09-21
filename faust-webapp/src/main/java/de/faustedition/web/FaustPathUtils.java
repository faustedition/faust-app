package de.faustedition.web;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

public class FaustPathUtils
{
	public static String getPath(HttpServletRequest request)
	{
		return FilenameUtils.normalize(StringUtils.strip(StringUtils.defaultString(request.getPathInfo()), "/"));
	}
}
