package de.abohnenkamp.paralipomena;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.faustedition.model.init.BootstrapPostProcessor;
import de.faustedition.model.manuscript.Facsimile;
import de.faustedition.model.manuscript.Manuscript;
import de.faustedition.model.manuscript.Portfolio;
import de.faustedition.model.manuscript.Repository;
import de.faustedition.model.manuscript.Transcription;
import de.faustedition.model.tei.TEIDocument;
import de.faustedition.model.tei.TEIDocumentManager;
import de.faustedition.util.ErrorUtil;
import de.faustedition.util.LoggingUtil;
import de.faustedition.util.XMLUtil;
import de.swkk.metadata.archivedb.ArchiveDatabase;
import de.swkk.metadata.archivedb.ArchiveDatabaseRecord;

public class ParalipomenaBootstrapPostProcessor implements BootstrapPostProcessor, InitializingBean
{
	private DissertationText dissertationText = new DissertationText();

	@Autowired
	private SessionFactory dbSessionFactory;

	private ArchiveDatabase archiveDatabase;

	@Autowired
	private TEIDocumentManager teiDocumentManager;

	@Override
	public void afterBootstrapping()
	{
		Session session = dbSessionFactory.getCurrentSession();
		Repository repository = Repository.find(session, "GSA");
		if (repository == null)
		{
			return;
		}

		List<ParalipomenonTranscription> paralipomena = dissertationText.extractParalipomena();
		for (ParalipomenonTranscription paralipomenon : paralipomena)
		{
			for (ArchiveDatabaseRecord record : archiveDatabase.collect(paralipomenon.getCallNumber()))
			{
				String portfolioNum = Integer.toString(record.getIdentNum());
				Portfolio portfolio = Portfolio.find(session, repository, portfolioNum);
				if (portfolio == null)
				{
					LoggingUtil.LOG.warn(String.format("Cannot find portfolio %s for paralipomenon %s", portfolioNum, paralipomenon.getCallNumber()));
					continue;
				}
				for (Manuscript manuscript : Manuscript.find(session, portfolio))
				{
					Facsimile facsimile = Facsimile.find(session, manuscript, manuscript.getName());
					if (facsimile == null)
					{
						continue;
					}
					Transcription transcription = Transcription.find(session, facsimile);
					if (transcription == null)
					{
						continue;
					}

					LoggingUtil.LOG.info(paralipomenon.getCallNumber() + " ===> " + portfolio.getName() + "/" + manuscript.getName());
					try
					{
						TEIDocument transcriptionDocument = transcription.buildTEIDocument(teiDocumentManager);
						Element textBodyElement = XMLUtil.getChild(transcriptionDocument.getTextElement(), "body");
						if (!XMLUtil.hasText(textBodyElement))
						{
							XMLUtil.removeChildren(textBodyElement);
						}

						for (Node node : XMLUtil.iterableNodeList(XMLUtil.getChild(paralipomenon.getText(), "text").getChildNodes()))
						{
							textBodyElement.appendChild(textBodyElement.getOwnerDocument().importNode(node, true));
						}

						transcription.update(transcriptionDocument);
					}
					catch (Exception e)
					{
						throw ErrorUtil.fatal(e, "Error while bootstrapping paralipomena data");
					}
					break;
				}
			}
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		archiveDatabase = new ArchiveDatabase();
	}
}
