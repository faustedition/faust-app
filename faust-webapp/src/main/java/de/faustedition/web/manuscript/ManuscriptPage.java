package de.faustedition.web.manuscript;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.request.InvalidUrlException;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.springframework.core.io.ClassPathResource;

import de.faustedition.model.manuscript.Facsimile;
import de.faustedition.model.manuscript.FacsimileImageResolution;
import de.faustedition.model.manuscript.Manuscript;
import de.faustedition.model.manuscript.Portfolio;
import de.faustedition.model.manuscript.Repository;
import de.faustedition.model.manuscript.Transcription;
import de.faustedition.util.ErrorUtil;
import de.faustedition.web.FaustApplication;
import de.faustedition.web.PageBase;
import de.faustedition.web.facsimile.FacsimileImage;
import de.faustedition.web.facsimile.FacsimileLink;
import de.faustedition.web.util.UpLink;
import de.faustedition.web.util.XMLSourcePanel;

public class ManuscriptPage extends PageBase
{
	private static final ClassPathResource TEI_2_HTML_XSL_RESOURCE = new ClassPathResource("ManuscriptTEI2HTML.xsl", ManuscriptPage.class);
	private static Templates tei2htmlTemplates;

	@SpringBean
	private SessionFactory dbSessionFactory;

	private Repository repository;
	private Portfolio portfolio;
	private Manuscript manuscript;
	private Facsimile facsimile;
	private Transcription transcription;

	public ManuscriptPage(PageParameters parameters)
	{
		super();

		final String repositoryName = parameters.getString("0");
		final String portfolioName = parameters.getString("1");
		final String transcriptionName = parameters.getString("2");
		if (repositoryName == null || portfolioName == null || transcriptionName == null)
		{
			throw new InvalidUrlException();
		}

		Session session = dbSessionFactory.getCurrentSession();
		FaustApplication.assertFound(repository = Repository.find(session, repositoryName));
		FaustApplication.assertFound(portfolio = Portfolio.find(session, repository, portfolioName));
		FaustApplication.assertFound(manuscript = Manuscript.find(session, portfolio, transcriptionName));
		FaustApplication.assertFound(facsimile = Facsimile.find(session, manuscript, transcriptionName));
		FaustApplication.assertFound(transcription = Transcription.find(session, facsimile));

		add(new Label("manuscriptHeader", new PropertyModel<String>(this, "manuscriptTitle")));
		add(new UpLink("portfolioLink", PortfolioPage.getLink("upLink", manuscript.getPortfolio())));

		FacsimileLink facsimileLink = new FacsimileLink("facsimileLink", facsimile, FacsimileImageResolution.LOW);
		facsimileLink.add(new FacsimileImage("facsimile", facsimile, FacsimileImageResolution.LOW));
		add(facsimileLink);
		
		add(new WebComponent("transcription")
		{
			@Override
			protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
			{
				try
				{
					ByteArrayOutputStream htmlStream = new ByteArrayOutputStream();
					tei2htmlTemplates.newTransformer().transform(new StreamSource(new ByteArrayInputStream(transcription.getTextData())), new StreamResult(htmlStream));
					replaceComponentTagBody(markupStream, openTag, new String(htmlStream.toByteArray(), "UTF-8"));
				}
				catch (TransformerException e)
				{
					throw ErrorUtil.fatal("XSL error while transforming manuscript transcription to HTML", e);
				}
				catch (IOException e)
				{
					throw ErrorUtil.fatal("I/O error while transforming manuscript transcription to HTML", e);
				}
			}
			
			@Override
			public boolean isVisible()
			{
				return transcription.hasText();
			}
		});

		add(new XMLSourcePanel("transcriptionSource", transcription.getTextData()));
	}

	public String getManuscriptTitle()
	{
		return manuscript.getPortfolio().getName() + " // " + manuscript.getName();
	}

	@Override
	public String getPageTitle()
	{
		return (manuscript == null ? "" : manuscript.getName());
	}

	public static BookmarkablePageLink<? extends Page> getLink(String id, Manuscript manuscript)
	{
		PageParameters parameters = new PageParameters();
		parameters.add("0", manuscript.getPortfolio().getRepository().getName());
		parameters.add("1", manuscript.getPortfolio().getName());
		parameters.add("2", manuscript.getName());
		return new BookmarkablePageLink<Page>(id, ManuscriptPage.class, parameters);
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
