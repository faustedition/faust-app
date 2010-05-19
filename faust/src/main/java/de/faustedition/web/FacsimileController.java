package de.faustedition.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.Sets;

import de.faustedition.facsimile.Facsimile;
import de.faustedition.facsimile.FacsimileProperties;
import de.faustedition.facsimile.FacsimileTileStore;
import de.faustedition.xml.XmlUtil;

@Controller
@RequestMapping("/facsimile")
public class FacsimileController implements InitializingBean {
	private static final String TMS_VERSION = "1.1.0";
	private static final String TMS_TITLE = "Digitale Faust-Edition :: Facsimile Tile Map Service";
	private static final int ZOOM_LEVELS = 5;
	private static final Logger LOG = LoggerFactory.getLogger(FacsimileController.class);
	private static final Set<String> IIP_PASSTHROUGH_HEADERS = Sets.newHashSet("Content-Type", "Date");
	@Autowired
	private FacsimileTileStore store;

	@Value("#{config['http.base']}")
	private String baseUrl;

	@Value("#{config['iipsrv.url']}")
	private String iipServerUrl;

	private HttpClient httpClient;

	@RequestMapping("/tms")
	public void tileMappingServiceRoot(HttpServletResponse response) throws IOException {
		Document descriptor = XmlUtil.documentBuilder().newDocument();
		Element services = descriptor.createElement("Services");
		descriptor.appendChild(services);

		Element service = descriptor.createElement("TileMapService");
		service.setAttribute("title", TMS_TITLE);
		service.setAttribute("version", TMS_VERSION);
		service.setAttribute("href", baseUrl + "facsimile/tms/" + TMS_VERSION + "/");
		services.appendChild(service);

		ControllerUtil.xmlResponse(descriptor, response, "text/xml");
	}

	@RequestMapping("/tms/" + TMS_VERSION)
	public void tileMappingService(HttpServletResponse response) throws Exception {
		Document descriptor = XmlUtil.documentBuilder().newDocument();

		Element service = descriptor.createElement("TileMapService");
		service.setAttribute("version", TMS_VERSION);
		service.setAttribute("services", baseUrl + "facsimile/tms/");
		descriptor.appendChild(service);

		Element title = descriptor.createElement("Title");
		service.appendChild(title);
		title.setTextContent(TMS_TITLE);

		Element description = descriptor.createElement("Abstract");
		service.appendChild(description);
		description.setTextContent(TMS_TITLE);

		Element tileMaps = descriptor.createElement("TileMaps");
		service.appendChild(tileMaps);

		URI base = new URI(baseUrl + "facsimile/tms/" + TMS_VERSION + "/map/");
		for (Facsimile facsimile : store.all()) {
			Element map = descriptor.createElement("TileMap");
			tileMaps.appendChild(map);

			map.setAttribute("title", facsimile.toUri().toString());
			map.setAttribute("srs", "n/a");
			map.setAttribute("profile", "local");
			map.setAttribute("href", base.resolve(facsimile.getPath()).toASCIIString());
		}

		ControllerUtil.xmlResponse(descriptor, response, "text/xml");
	}

	@RequestMapping("/tms/1.1.0/map/**")
	public void tileMap(HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String path = ControllerUtil.getPath(request, "facsimile/tms/" + TMS_VERSION + "/map");
		final Facsimile facsimile = new Facsimile(path);
		if (path.length() == 0 || store.facsimile(facsimile) == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		FacsimileProperties properties = store.properties(facsimile);

		Document descriptor = XmlUtil.documentBuilder().newDocument();

		Element map = descriptor.createElement("TileMap");
		map.setAttribute("version", TMS_VERSION);
		map.setAttribute("tilemapservice", baseUrl + "tms/" + TMS_VERSION + "/");
		descriptor.appendChild(map);

		Element title = descriptor.createElement("Title");
		map.appendChild(title);
		title.setTextContent(facsimile.toUri().toString());

		Element description = descriptor.createElement("Abstract");
		map.appendChild(description);
		description.setTextContent(facsimile.toUri().toString());

		Element srs = descriptor.createElement("SRS");
		map.appendChild(srs);
		srs.setTextContent("n/a");

		Element boundingBox = descriptor.createElement("BoundingBox");
		boundingBox.setAttribute("minx", "0");
		boundingBox.setAttribute("miny", "0");
		boundingBox.setAttribute("maxx", Integer.toString(properties.getWidth()));
		boundingBox.setAttribute("maxy", Integer.toString(properties.getHeight()));
		map.appendChild(boundingBox);

		Element origin = descriptor.createElement("Origin");
		origin.setAttribute("x", "0");
		origin.setAttribute("y", "0");
		map.appendChild(origin);

		Element format = descriptor.createElement("TileFormat");
		format.setAttribute("width", "256");
		format.setAttribute("height", "256");
		format.setAttribute("mime-type", "image/jpeg");
		format.setAttribute("extension", "jpg");
		map.appendChild(format);

		Element tileSets = descriptor.createElement("TileSets");
		tileSets.setAttribute("profile", "local");
		map.appendChild(tileSets);

		URI base = new URI(baseUrl + "facsimile/tms/" + TMS_VERSION + "/tile/" + facsimile.getPath() + "/");
		for (int level = ZOOM_LEVELS; level > 0; level--) {
			String units = Integer.toString((int) Math.pow(2, level - 1));
			Element tileSet = descriptor.createElement("TileSet");
			tileSet.setAttribute("href", base.resolve(units).toASCIIString());
			tileSet.setAttribute("units-per-pixel", units);
			tileSet.setAttribute("order", Integer.toString(ZOOM_LEVELS - level));
			tileSets.appendChild(tileSet);
		}

		ControllerUtil.xmlResponse(descriptor, response, "text/xml");
	}

	@RequestMapping("/iip")
	public void iipProxy(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
	public void stream(WebRequest webRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
		final String path = StringUtils.removeEnd(ControllerUtil.getPath(request, "facsimile"), FacsimileTileStore.TIF_EXTENSION);
		if (path.length() == 0) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		final Facsimile facsimile = new Facsimile(path);
		LOG.debug("Retrieving {}", facsimile);

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
