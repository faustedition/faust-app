package de.faustedition.web;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import de.faustedition.model.ObjectNotFoundException;

public class ControllerUtil
{
	public static String getPath(HttpServletRequest request)
	{
		return FilenameUtils.normalize(StringUtils.strip(StringUtils.defaultString(request.getPathInfo()), "/"));
	}

	public static Deque<String> getPathComponents(HttpServletRequest request)
	{
		return new ArrayDeque<String>(Arrays.asList(StringUtils.split(ControllerUtil.getPath(request), "/")));
	}

	public static <T> T foundObject(T object) throws ObjectNotFoundException
	{
		if (object == null)
		{
			throw new ObjectNotFoundException();
		}
		return object;
	}
}
