package de.faustedition.web.manuscript;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Deque;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import de.faustedition.model.ObjectNotFoundException;
import de.faustedition.model.manuscript.Facsimile;
import de.faustedition.model.manuscript.Manuscript;
import de.faustedition.model.manuscript.Portfolio;
import de.faustedition.model.manuscript.Repository;
import de.faustedition.model.manuscript.Transcription;
import de.faustedition.model.metadata.MetadataAssignment;
import de.faustedition.util.ErrorUtil;
import de.faustedition.util.XMLUtil;
import de.faustedition.web.ControllerUtil;
import de.faustedition.web.metadata.MetadataTable;

@Controller
public class ManuscriptController implements MessageSourceAware
{
	private static final ClassPathResource TEI_2_HTML_XSL_RESOURCE = new ClassPathResource("/manuscript-tei-2-xhtml.xsl");
	private static Templates tei2htmlTemplates;

	@Autowired
	private SessionFactory sessionFactory;
	private MessageSource messageSource;

	@Override
	public void setMessageSource(MessageSource messageSource)
	{
		this.messageSource = messageSource;
	}

	@RequestMapping("/manuscripts/**")
	public String browse(HttpServletRequest request, ModelMap model, Locale locale) throws TransformerException, IOException, ObjectNotFoundException
	{
		Deque<String> pathComponents = ControllerUtil.getPathComponents(request);
		if (!pathComponents.isEmpty() && "manuscripts".equals(pathComponents.getFirst()))
		{
			pathComponents.removeFirst();
		}

		Session session = sessionFactory.getCurrentSession();
		Repository repository = null;
		Portfolio portfolio = null;
		Manuscript manuscript = null;

		if (!pathComponents.isEmpty())
		{
			model.addAttribute(ControllerUtil.foundObject(repository = Repository.find(session, pathComponents.pop())));
		}
		if (!pathComponents.isEmpty())
		{
			model.addAttribute(ControllerUtil.foundObject(portfolio = Portfolio.find(session, repository, pathComponents.pop())));
		}
		if (!pathComponents.isEmpty())
		{
			model.addAttribute(ControllerUtil.foundObject(manuscript = Manuscript.find(session, portfolio, pathComponents.pop())));
		}

		if (manuscript != null)
		{
			return browseManuscript(manuscript, model);
		}
		else if (portfolio != null)
		{
			return browsePortfolio(portfolio, model, locale);
		}
		else if (repository != null)
		{
			return browseRepository(repository, model);
		}
		else
		{
			return browse(model);
		}

	}

	private String browse(ModelMap model)
	{
		model.addAttribute(Repository.find(sessionFactory.getCurrentSession()));
		return "manuscripts/index";
	}

	private String browseRepository(Repository repository, ModelMap model)
	{
		model.addAttribute(repository).addAttribute(Portfolio.find(sessionFactory.getCurrentSession(), repository));
		return "manuscripts/repository";
	}

	private String browsePortfolio(Portfolio portfolio, ModelMap model, Locale locale)
	{
		Session session = sessionFactory.getCurrentSession();
		model.addAttribute(new MetadataTable(MetadataAssignment.find(session, Portfolio.class.getName(), portfolio.getId()), messageSource, locale));
		model.addAttribute(Manuscript.find(session, portfolio));
		return "manuscripts/portfolio";
	}

	private String browseManuscript(Manuscript manuscript, ModelMap model) throws TransformerException, IOException
	{
		Session session = sessionFactory.getCurrentSession();
		Facsimile facsimile = Facsimile.find(session, manuscript, manuscript.getName());
		Transcription transcription = Transcription.find(session, facsimile);
		model.addAttribute(facsimile).addAttribute(transcription);

		StringWriter htmlTranscription = new StringWriter();
		tei2htmlTemplates.newTransformer().transform(new StreamSource(new ByteArrayInputStream(transcription.getTextData())), new StreamResult(htmlTranscription));
		model.addAttribute("htmlTranscription", htmlTranscription.toString());
		model.addAttribute("transcriptionSource", new String(XMLUtil.serialize(XMLUtil.parse(transcription.getTextData()), true), "UTF-8"));

		return "manuscripts/manuscript";
	}

	static
	{
		try
		{
			tei2htmlTemplates = TransformerFactory.newInstance().newTemplates(new StreamSource(TEI_2_HTML_XSL_RESOURCE.getInputStream()));
		}
		catch (TransformerException e)
		{
			throw ErrorUtil.fatal("XSL error while compiling TEI/HTML conversion template", e);
		}
		catch (IOException e)
		{
			throw ErrorUtil.fatal("I/O error while compiling TEI/HTML conversion template", e);
		}
	}

}
