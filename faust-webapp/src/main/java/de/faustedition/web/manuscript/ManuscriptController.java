package de.faustedition.web.manuscript;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.Deque;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringUtils;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
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
	private static final ClassPathResource TEI_2_XSLFO_XSL_RESOURCE = new ClassPathResource("/manuscript-tei-2-xsl-fo.xsl");
	private static final ClassPathResource TEI_2_SVG_XSL_RESOURCE = new ClassPathResource("/manuscript-tei-2-svg.xsl");
	private static final FopFactory FOP_FACTORY = FopFactory.newInstance();
	private static Templates tei2htmlTemplates;
	private static Templates tei2xslFoTemplates;
	private static Templates tei2SvgTemplates;


	@Autowired
	private SessionFactory sessionFactory;

	private MessageSource messageSource;

	@Override
	public void setMessageSource(MessageSource messageSource)
	{
		this.messageSource = messageSource;
	}

	@RequestMapping("/manuscripts/**")
	public String browse(HttpServletRequest request, HttpServletResponse response, ModelMap model, Locale locale) throws TransformerException, IOException,
			ObjectNotFoundException, FOPException
	{
		Deque<String> pathComponents = ControllerUtil.getPathComponents(request);
		if (!pathComponents.isEmpty() && "manuscripts".equals(pathComponents.getFirst()))
		{
			pathComponents.removeFirst();
		}

		Session session = sessionFactory.getCurrentSession();
		Repository repository = null;
		Portfolio portfolio = null;

		if (!pathComponents.isEmpty())
		{
			model.addAttribute(ControllerUtil.foundObject(repository = Repository.find(session, pathComponents.pop())));
		}
		if (!pathComponents.isEmpty())
		{
			model.addAttribute(ControllerUtil.foundObject(portfolio = Portfolio.find(session, repository, pathComponents.pop())));
		}

		if (portfolio != null && !pathComponents.isEmpty())
		{
			return browseManuscript(portfolio, pathComponents.pop(), model, response);
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

	private String browseManuscript(Portfolio portfolio, String manuscriptPath, ModelMap model, HttpServletResponse response) throws TransformerException, IOException,
			ObjectNotFoundException, FOPException
	{
		Session session = sessionFactory.getCurrentSession();
		Manuscript manuscript = null;

		if (StringUtils.endsWithIgnoreCase(manuscriptPath, ".pdf"))
		{
			manuscriptPath = StringUtils.removeEndIgnoreCase(manuscriptPath, ".pdf");
			manuscript = ControllerUtil.foundObject(Manuscript.find(session, portfolio, manuscriptPath));
			return streamPdfDocument(manuscript, response);

		}

		if (StringUtils.endsWithIgnoreCase(manuscriptPath, ".svg"))
		{
			manuscriptPath = StringUtils.removeEndIgnoreCase(manuscriptPath, ".svg");
			manuscript = ControllerUtil.foundObject(Manuscript.find(session, portfolio, manuscriptPath));
			return streamSvgDocument(manuscript, response);

		}

		model.addAttribute(manuscript = ControllerUtil.foundObject(Manuscript.find(session, portfolio, manuscriptPath)));
		Facsimile facsimile = Facsimile.find(session, manuscript, manuscript.getName());
		Transcription transcription = Transcription.find(session, facsimile);
		model.addAttribute(facsimile).addAttribute(transcription);

		StringWriter htmlTranscription = new StringWriter();
		tei2htmlTemplates.newTransformer().transform(new StreamSource(new ByteArrayInputStream(transcription.getTextData())), new StreamResult(htmlTranscription));
		model.addAttribute("htmlTranscription", htmlTranscription.toString());
		model.addAttribute("transcriptionSource", new String(XMLUtil.serialize(XMLUtil.parse(transcription.getTextData()), true), "UTF-8"));

		return "manuscripts/manuscript";
	}

	private String streamSvgDocument(Manuscript manuscript, HttpServletResponse response) throws TransformerException, IOException
	{
		Session session = sessionFactory.getCurrentSession();
		Facsimile facsimile = Facsimile.find(session, manuscript, manuscript.getName());
		Transcription transcription = Transcription.find(session, facsimile);
		response.setContentType("image/svg+xml");
		ServletOutputStream outputStream = response.getOutputStream();
		tei2SvgTemplates.newTransformer().transform(new StreamSource(new ByteArrayInputStream(transcription.getTextData())), new StreamResult(outputStream));
		outputStream.flush();
		return null;
	}

	private String streamPdfDocument(Manuscript manuscript, HttpServletResponse response) throws TransformerException, IOException, FOPException
	{
		Session session = sessionFactory.getCurrentSession();
		Facsimile facsimile = Facsimile.find(session, manuscript, manuscript.getName());
		Transcription transcription = Transcription.find(session, facsimile);
		response.setContentType(MimeConstants.MIME_PDF);
		ServletOutputStream outputStream = response.getOutputStream();
		FOUserAgent userAgent = new FOUserAgent(FOP_FACTORY);
		userAgent.setTitle("Digitale Faust-Edition :: " + manuscript.getName());
		userAgent.setAuthor("Johann Wolfgang von Goethe");
		userAgent.setCreator("Digitale Faust-Edition");
		userAgent.setCreationDate(new Date());
		userAgent.setProducer("Digitale Faust-Edition");
		Fop fop = FOP_FACTORY.newFop(MimeConstants.MIME_PDF, userAgent, outputStream);
		
		tei2xslFoTemplates.newTransformer().transform(new StreamSource(new ByteArrayInputStream(transcription.getTextData())), new SAXResult(fop.getDefaultHandler()));
		outputStream.flush();
		return null;
	}

	static
	{
		try
		{
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			tei2htmlTemplates = transformerFactory.newTemplates(new StreamSource(TEI_2_HTML_XSL_RESOURCE.getInputStream()));
			tei2xslFoTemplates = transformerFactory.newTemplates(new StreamSource(TEI_2_XSLFO_XSL_RESOURCE.getInputStream()));
			tei2SvgTemplates = transformerFactory.newTemplates(new StreamSource(TEI_2_SVG_XSL_RESOURCE.getInputStream()));
		}
		catch (TransformerException e)
		{
			throw ErrorUtil.fatal("XSL error while compiling TEI conversion templates", e);
		}
		catch (IOException e)
		{
			throw ErrorUtil.fatal("I/O error while compiling TEI conversion templates", e);
		}
	}

}
