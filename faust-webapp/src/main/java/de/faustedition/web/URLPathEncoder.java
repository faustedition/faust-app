package de.faustedition.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Pattern;

import de.faustedition.util.ErrorUtil;

import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

public class URLPathEncoder implements TemplateMethodModel
{

	private static final String UTF8_ENCODED_SLASH_PATTERN = Pattern.quote("%2F");

	@SuppressWarnings("unchecked")
	@Override
	public Object exec(List arguments) throws TemplateModelException
	{
		if (arguments.size() != 1)
		{
			throw new TemplateModelException("Please provide a path to encode");
		}

		return encode((String) arguments.get(0));
	}

	public static String encode(String path)
	{
		try
		{
			return URLEncoder.encode(path, "UTF-8").replaceAll(UTF8_ENCODED_SLASH_PATTERN, "/");
		}
		catch (UnsupportedEncodingException e)
		{
			throw ErrorUtil.fatal("UTF-8 encoding unsupported", e);
		}
	}

}
