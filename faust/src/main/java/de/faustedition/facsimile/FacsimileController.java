package de.faustedition.facsimile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

import de.faustedition.web.ControllerUtil;

@Controller
public class FacsimileController {
	private static final Logger LOG = LoggerFactory.getLogger(FacsimileController.class);

	@Autowired
	private FacsimileManager facsimileManager;

	@RequestMapping("/facsimile/**")
	public void stream(WebRequest webRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
		String path = ControllerUtil.getPath(request, "facsimile");
		if (path.length() == 0) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		FacsimileResolution resolution = null;
		String filename = FilenameUtils.getName(path);
		for (FacsimileResolution candidate : FacsimileResolution.values()) {
			if (candidate.matches(filename)) {
				resolution = candidate;
				path = StringUtils.removeEnd(path, resolution.getSuffix());
				break;
			}
		}
		if (resolution == null || resolution == FacsimileResolution.HIGH) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		LOG.debug("Retrieving facsimile '{}' with resolution {}", path, resolution);
		File file = facsimileManager.find(path, resolution);
		if (file == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		response.setContentType(resolution.getMimeType());
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
}
