package de.faustedition.web;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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

import de.faustedition.model.Facsimile;
import de.faustedition.model.Transcription;
import de.faustedition.model.facsimile.FacsimileStore;
import de.faustedition.model.store.ContentContainer;
import de.faustedition.model.store.ObjectNotFoundException;
import de.faustedition.model.transcription.TranscriptionStore;

@Controller
public class FacsimileController {
	@Autowired
	private TranscriptionStore transcriptionStore;

	@Autowired
	private FacsimileStore facsimileStore;

	@RequestMapping("/facsimile/**")
	public void stream(WebRequest webRequest, HttpServletRequest request, HttpServletResponse response) throws RepositoryException, ObjectNotFoundException, IOException {
		String path = StringUtils.defaultString(request.getPathInfo());
		List<ContentContainer> traversalList = transcriptionStore.traverse(path);

		if (traversalList.size() < 3) {
			throw new ObjectNotFoundException(path);
		}

		Facsimile facsimile = facsimileStore.find((Transcription) traversalList.get(2));
		if (facsimile == null) {
			throw new ObjectNotFoundException(path);
		}

		response.setContentType("image/jpeg");
		response.setContentLength((int) facsimile.getImageFile().length());

		if (webRequest.checkNotModified(facsimile.getImageFile().lastModified())) {
			return;
		}

		ServletOutputStream outputStream = response.getOutputStream();
		InputStream facsimileStream = null;
		try {
			facsimileStream = new FileInputStream(facsimile.getImageFile());
			IOUtils.copy(facsimileStream, outputStream);
			outputStream.flush();
		} finally {
			IOUtils.closeQuietly(facsimileStream);
		}
	}
}
