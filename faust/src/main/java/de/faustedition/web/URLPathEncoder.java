package de.faustedition.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import de.faustedition.ErrorUtil;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.DeepUnwrap;

public class URLPathEncoder implements TemplateMethodModelEx {

	private static final String UTF8_ENCODED_SLASH_PATTERN = Pattern.quote("%2F");

	@SuppressWarnings("unchecked")
	public Object exec(List arguments) throws TemplateModelException {
		if (arguments.size() != 1) {
			throw new TemplateModelException("Please provide a path to encode");
		}

		Object path = DeepUnwrap.unwrap((TemplateModel) arguments.get(0));
		return (path instanceof Collection ? encode((Collection<String>) path) : encode(path.toString()));
	}

	public static String encode(String path) {
		try {
			return URLEncoder.encode(path, "UTF-8").replaceAll(UTF8_ENCODED_SLASH_PATTERN, "/");
		} catch (UnsupportedEncodingException e) {
			throw ErrorUtil.fatal(e, "UTF-8 encoding unsupported");
		}
	}

	public static String encode(Collection<String> pathComponents) {
		try {
			StringBuilder path = new StringBuilder();
			for (String pc : pathComponents) {
				path.append((path.length() == 0 ? "" : "/") + URLEncoder.encode(pc, "UTF-8"));
			}
			return path.toString();
		} catch (UnsupportedEncodingException e) {
			throw ErrorUtil.fatal(e, "UTF-8 encoding unsupported");
		}
	}

}
