package de.faustedition.web;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import de.faustedition.model.store.ContentObject;
import de.faustedition.model.store.ObjectNotFoundException;
import de.faustedition.model.transcription.Portfolio;
import de.faustedition.model.transcription.Repository;
import de.faustedition.model.transcription.Transcription;
import de.faustedition.model.transcription.TranscriptionStore;
import de.faustedition.util.LoggingUtil;

@Controller
public class ManuscriptController extends AbstractTranscriptionBasedController {
	@RequestMapping("/manuscripts/**")
	public ModelAndView browse(HttpServletRequest request, HttpServletResponse response) throws RepositoryException, ObjectNotFoundException, IOException {
		String requestURL = request.getRequestURL().toString();
		if (!requestURL.endsWith("/")) {
			response.sendRedirect(requestURL + "/");
			return null;
		}

		TranscriptionStore transcriptionStore = getTranscriptionStore();
		ModelMap model = new ModelMap().addAttribute(transcriptionStore);
		
		boolean teiDocumentMode = false;
		String path = transcriptionStore.buildAbsolutePath(getPath(request));
		if (path.endsWith("/tei-document")) {
			path = StringUtils.removeEnd(path, "/tei-document");
			teiDocumentMode = true;
		}

		ContentObject contentObject = contentStore.get(path);
		if (contentObject == null) {
			throw new ObjectNotFoundException(path);
		}

		if (contentObject instanceof TranscriptionStore) {
			return new ModelAndView("manuscripts/store", model.addAttribute(transcriptionStore.findRepositories(contentStore)));
		}

		if (contentObject instanceof Repository) {
			Repository repository = (Repository) contentObject;
			return new ModelAndView("manuscripts/repository", model.addAttribute(repository).addAttribute(repository.findPortfolios(contentStore)));
		}

		if (contentObject instanceof Portfolio) {
			Portfolio portfolio = (Portfolio) contentObject;
			return new ModelAndView("manuscripts/portfolio", model.addAttribute(portfolio).addAttribute(portfolio.findTranscriptions(contentStore)));

		}

		if (contentObject instanceof Transcription) {
			Transcription transcription = (Transcription) contentObject;

			if (teiDocumentMode) {
				byte[] transcriptionData = transcription.retrieve(contentStore);

				response.setContentType("application/xml");
				response.setContentLength(transcriptionData.length);

				ServletOutputStream outputStream = response.getOutputStream();
				IOUtils.write(transcriptionData, outputStream);
				outputStream.flush();

				return null;
			}

			model.addAttribute(transcription).addAttribute("facsimilePath", transcriptionStore.buildRelativePath(transcription.getPath()));
			return new ModelAndView("manuscripts/manuscript", model);
		}

		LoggingUtil.LOG.warn(String.format("Unknown content object addressed in manuscript controller: %s", contentObject));
		throw new ObjectNotFoundException(path);
	}
}
