package de.abohnenkamp.paralipomena;

import java.util.List;

import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.util.NodeListIterable;

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
import de.faustedition.model.manuscript.TranscriptionDocument;
import de.faustedition.model.manuscript.TranscriptionDocumentFactory;
import de.faustedition.util.LoggingUtil;
import de.swkk.metadata.archivedb.ArchiveDatabase;
import de.swkk.metadata.archivedb.ArchiveDatabaseRecord;

public class ParalipomenaBootstrapPostProcessor implements BootstrapPostProcessor, InitializingBean
{
	private DissertationText dissertationText = new DissertationText();

	@Autowired
	private SessionFactory dbSessionFactory;

	private ArchiveDatabase archiveDatabase;

	@Override
	public void afterBootstrapping()
	{
		Session session = dbSessionFactory.getCurrentSession();
		Repository repository = Repository.find(session, "GSA");
		if (repository == null)
		{
			return;
		}

		TranscriptionDocumentFactory transcriptionDocumentFactory = new TranscriptionDocumentFactory();
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
					TranscriptionDocument transcriptionDocument = transcriptionDocumentFactory.build(transcription);
					Element textBodyElement = DomUtil.getChild(transcriptionDocument.getTextElement(), "body");
					if (!transcriptionDocument.hasText())
					{
						for (Node node : DomUtil.getChildren(textBodyElement))
						{
							textBodyElement.removeChild(node);
						}
					}

					for (Node node : new NodeListIterable(DomUtil.getChild(paralipomenon.getText(), "text").getChildNodes()))
					{
						textBodyElement.appendChild(textBodyElement.getOwnerDocument().importNode(node, true));
					}

					transcriptionDocument.update(transcription);
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
