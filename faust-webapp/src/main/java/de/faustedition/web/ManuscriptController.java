package de.faustedition.web;

import java.io.IOException;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import de.faustedition.model.Portfolio;
import de.faustedition.model.Repository;
import de.faustedition.model.Transcription;
import de.faustedition.model.store.ContentContainer;
import de.faustedition.model.store.ObjectNotFoundException;
import de.faustedition.model.transcription.TranscriptionStore;

@Controller
public class ManuscriptController {

	@Autowired
	private TranscriptionStore transcriptionStore;

	@RequestMapping("/manuscripts/**")
	public ModelAndView browse(HttpServletRequest request, HttpServletResponse response) throws RepositoryException, ObjectNotFoundException, IOException {
		String requestURL = request.getRequestURL().toString();
		if (!requestURL.endsWith("/")) {
			response.sendRedirect(requestURL + "/");
			return null;
		}

		String path = StringUtils.strip(StringUtils.defaultString(request.getPathInfo()), "/");
		List<ContentContainer> traversalList = transcriptionStore.traverse(path);

		if (traversalList.size() < 1) {
			return new ModelAndView("manuscripts/store", new ModelMap(transcriptionStore.findRepositories()));
		}

		if (traversalList.size() < 2) {
			Repository repository = (Repository) traversalList.get(0);
			ModelMap repositoryModel = new ModelMap(repository).addAttribute(transcriptionStore.findPortfolios(repository));
			return new ModelAndView("manuscripts/repository", repositoryModel);
		}

		if (traversalList.size() < 3) {
			Portfolio portfolio = (Portfolio) traversalList.get(1);
			ModelMap portfolioModel = new ModelMap(portfolio).addAttribute(transcriptionStore.findTranscriptions(portfolio));
			return new ModelAndView("manuscripts/portfolio", portfolioModel);
		}

		Transcription transcription = (Transcription) traversalList.get(2);
		
		if ("tei-document".equalsIgnoreCase(StringUtils.substringAfterLast(path, "/"))) {
			byte[] transcriptionData = transcriptionStore.retrieve(transcription);
			
			response.setContentType("application/xml");
			response.setContentLength(transcriptionData.length);
			
			ServletOutputStream outputStream = response.getOutputStream();
			IOUtils.write(transcriptionData, outputStream);
			outputStream.flush();
			
			return null;
		}
		
		ModelMap manuscriptModel = new ModelMap(transcription);
		return new ModelAndView("manuscripts/manuscript", manuscriptModel);
	}
}
