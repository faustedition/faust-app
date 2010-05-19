package de.faustedition.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

import de.faustedition.xml.XmlUtil;

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

	public static void xmlResponse(Document document, HttpServletResponse response) throws IOException {
		xmlResponse(document, response, "application/xml");
	}

	public static void xmlResponse(Document document, HttpServletResponse response, String contentType) throws IOException {
		response.setContentType(contentType);
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		XmlUtil.serialize(document, out);
		out.flush();
	}
}
