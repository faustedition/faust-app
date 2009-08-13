package de.faustedition.web;

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

import de.faustedition.model.facsimile.Facsimile;
import de.faustedition.model.facsimile.FacsimileStore;
import de.faustedition.model.facsimile.FacsimileStore.Resolution;
import de.faustedition.model.store.ContentObject;
import de.faustedition.model.store.ObjectNotFoundException;
import de.faustedition.model.transcription.Transcription;

@Controller
public class FacsimileController extends AbstractTranscriptionBasedController {
	@Autowired
	private FacsimileStore facsimileStore;

	@RequestMapping("/facsimile/**")
	public void stream(WebRequest webRequest, HttpServletRequest request, HttpServletResponse response) throws RepositoryException, ObjectNotFoundException, IOException {
		String facsimilePath = getPath(request);

		Resolution facsimileResolution = Resolution.LOW;
		if (facsimilePath.endsWith("/thumb")) {
			facsimilePath = StringUtils.removeEndIgnoreCase(facsimilePath, "/thumb");
			facsimileResolution = Resolution.THUMB;
		}

		facsimilePath = getTranscriptionStore().buildAbsolutePath(facsimilePath);
		ContentObject contentObject = contentStore.get(facsimilePath);
		if (contentObject == null || !(contentObject instanceof Transcription)) {
			throw new ObjectNotFoundException(facsimilePath);
		}

		final Facsimile facsimile = facsimileStore.find((Transcription) contentObject, facsimileResolution);
		if (facsimile == null) {
			throw new ObjectNotFoundException(facsimilePath);
		}

		response.setContentType("image/jpeg");
		response.setContentLength((int) facsimile.getImageFile().length());

		if (webRequest.checkNotModified(facsimile.getImageFile().lastModified())) {
			return;
		}

		ServletOutputStream responseStream = response.getOutputStream();
		InputStream imageStream = null;
		try {
			IOUtils.copy(imageStream = new FileInputStream(facsimile.getImageFile()), responseStream);
			responseStream.flush();
		} finally {
			IOUtils.closeQuietly(imageStream);
		}
	}
}
