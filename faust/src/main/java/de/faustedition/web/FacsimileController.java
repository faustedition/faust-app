package de.faustedition.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

import com.google.common.collect.Sets;

import de.faustedition.Log;
import de.faustedition.facsimile.Facsimile;
import de.faustedition.facsimile.FacsimileStore;

@Controller
@RequestMapping("/facsimile")
public class FacsimileController implements InitializingBean {
	private static final Set<String> IIP_PASSTHROUGH_HEADERS = Sets.newHashSet("Content-Type", "Date");

	@Value("#{config['iipsrv.url']}")
	private String iipServerUrl;

	@Autowired
	private FacsimileStore store;

	private HttpClient httpClient;

	@RequestMapping("/iip")
	public void proxyIIP(HttpServletRequest request, HttpServletResponse response) throws Exception {
		GetMethod get = new GetMethod(iipServerUrl);
		try {
			InputStream in = null;
			try {
				get.setQueryString(request.getQueryString());

				int statusCode = httpClient.executeMethod(get);
				if (statusCode != HttpStatus.SC_OK) {
					response.sendError(statusCode, get.getStatusText());
					IOUtils.toByteArray(in = get.getResponseBodyAsStream());
				} else {
					for (Header header : get.getResponseHeaders()) {
						if (IIP_PASSTHROUGH_HEADERS.contains(header.getName())) {
							response.setHeader(header.getName(), header.getValue());
						}
					}

					ServletOutputStream out = response.getOutputStream();
					IOUtils.copy(in = get.getResponseBodyAsStream(), out);
					out.flush();
				}
			} finally {
				IOUtils.closeQuietly(in);
			}
		} finally {
			get.releaseConnection();
		}
	}

	@RequestMapping("/**")
	public void streamFacsimile(WebRequest webRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
		final String path = StringUtils.removeEnd(ControllerUtil.getPath(request, "facsimile"), FacsimileStore.TIF_EXTENSION);
		if (path.length() == 0) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		final Facsimile facsimile = new Facsimile(path);
		Log.LOGGER.debug("Retrieving {}", facsimile);

		File file = store.facsimile(facsimile);
		if (file == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		response.setContentType("image/tiff");
		response.setContentLength((int) file.length());

		if (webRequest.checkNotModified(file.lastModified())) {
			return;
		}

		InputStream imageStream = null;
		try {
			IOUtils.copy(imageStream = new FileInputStream(file), response.getOutputStream());
		} finally {
			IOUtils.closeQuietly(imageStream);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		connectionManager.getParams().setDefaultMaxConnectionsPerHost(10);
		httpClient = new HttpClient(connectionManager);
	}
}
