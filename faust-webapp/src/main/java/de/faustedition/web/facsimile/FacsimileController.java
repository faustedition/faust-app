package de.faustedition.web.facsimile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.jcr.RepositoryException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

import de.faustedition.model.facsimile.FacsimileStore;
import de.faustedition.model.facsimile.FacsimileResolution;
import de.faustedition.model.store.ContentStore;
import de.faustedition.model.store.ObjectNotFoundException;
import de.faustedition.web.FaustPathUtils;

@Controller
public class FacsimileController {
	public static final String URL_PREFIX = "facsimile";
	@Autowired
	private FacsimileStore facsimileStore;

	@RequestMapping("/" + URL_PREFIX + "/**")
	public void stream(WebRequest webRequest, HttpServletRequest request, HttpServletResponse response) throws RepositoryException, ObjectNotFoundException, IOException {
		String path = FaustPathUtils.getPath(request);

		FacsimileResolution facsimileResolution = null;
		for (FacsimileResolution resolution : FacsimileResolution.values()) {
			if (resolution.matches(path)) {
				facsimileResolution = resolution;
				path = StringUtils.removeEnd(path, resolution.getSuffix());
			}
		}

		if (facsimileResolution == null || facsimileResolution == FacsimileResolution.HIGH) {
			throw new ObjectNotFoundException();
		}
		
		path = ContentStore.normalizePath(path);
		final File facsimile = facsimileStore.find(path, facsimileResolution);
		if (facsimile == null) {
			throw new ObjectNotFoundException(path);
		}

		response.setContentType(facsimileResolution.getMimeType());
		response.setContentLength((int) facsimile.length());

		if (webRequest.checkNotModified(facsimile.lastModified())) {
			return;
		}

		ServletOutputStream responseStream = response.getOutputStream();
		InputStream imageStream = null;
		try {
			IOUtils.copy(imageStream = new FileInputStream(facsimile), responseStream);
			responseStream.flush();
		} finally {
			IOUtils.closeQuietly(imageStream);
		}
	}
}
