package de.faustedition.model.dav;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import net.sf.practicalxml.ParseUtil;
import net.sf.practicalxml.XmlUtil;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.google.common.base.Preconditions;

import de.faustedition.model.manuscript.Facsimile;
import de.faustedition.model.manuscript.Manuscript;
import de.faustedition.model.manuscript.Transcription;
import de.faustedition.model.tei.TEIDocument;
import de.faustedition.util.XMLUtil;

public class TranscriptionDavResource extends DavResourceBase implements GetableResource, PropFindableResource
{
	private static final String CSS_STYLE_SHEET_PATH = "/schema/faust.css";
	private static final String RNG_SCHEMA_PATH = "/schema/faust.rnc";

	private final Manuscript manuscript;
	private Transcription transcription;
	private TEIDocument transcriptionDocument;
	private byte[] transcriptionDocumentData;

	protected TranscriptionDavResource(DavResourceFactory factory, Manuscript manuscript)
	{
		super(factory);
		this.manuscript = manuscript;
	}

	@Override
	public String getName()
	{
		return manuscript.getPortfolio().getName() + "_" + manuscript.getName() + ".xml";
	}

	@Override
	public Date getCreateDate()
	{
		return getTranscription().getCreated();
	}

	@Override
	public Date getModifiedDate()
	{
		return getTranscription().getLastModified();
	}

	protected TEIDocument getTranscriptionDocument()
	{
		if (transcriptionDocument == null)
		{
			transcriptionDocument = getTranscription().buildTEIDocument(factory.getTeiDocumentManager());

			Document document = transcriptionDocument.getDocument();
			String cssStylesheetUri = XmlUtil.escape(factory.getBaseURI() + CSS_STYLE_SHEET_PATH);
			String rngSchemaUri = XmlUtil.escape(factory.getBaseURI() + RNG_SCHEMA_PATH);
			document.insertBefore(document.createProcessingInstruction("xml-stylesheet", String.format("href=\"%s\" type=\"text/css\"", cssStylesheetUri)), document.getDocumentElement());
			document.insertBefore(document.createProcessingInstruction("oxygen", String.format("RNGSchema=\"%s\" type=\"compact\"", rngSchemaUri)), document.getDocumentElement());
		}

		return transcriptionDocument;

	}

	protected Transcription getTranscription()
	{
		if (transcription == null)
		{
			Facsimile facsimile = Facsimile.find(factory.getDbSessionFactory().getCurrentSession(), manuscript, manuscript.getName());
			Preconditions.checkNotNull(facsimile);
			transcription = Transcription.find(factory.getDbSessionFactory().getCurrentSession(), facsimile);
			Preconditions.checkNotNull(transcription);
		}
		return transcription;
	}

	public byte[] getTranscriptionDocumentData()
	{
		if (transcriptionDocumentData == null)
		{
			ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			XMLUtil.serialize(getTranscriptionDocument().getDocument(), dataStream, true);
			transcriptionDocumentData = dataStream.toByteArray();
		}

		return transcriptionDocumentData;
	}

	@Override
	public Long getContentLength()
	{
		return Long.valueOf(getTranscriptionDocumentData().length);
	}

	@Override
	public String getContentType(String accepts)
	{
		return "application/xml";
	}

	@Override
	public Long getMaxAgeSeconds(Auth auth)
	{
		return null;
	}

	@Override
	public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException
	{
		if (transcriptionDocumentData == null)
		{
			XMLUtil.serialize(getTranscriptionDocument().getDocument(), out, true);
		}
		else
		{
			IOUtils.write(getTranscriptionDocumentData(), out);
		}
		out.flush();
	}

	@Override
	public Object getLockResource()
	{
		return manuscript;
	}

	public void update(InputStream inputStream) throws IOException
	{
		getTranscription().update(new TEIDocument(ParseUtil.parse(new InputSource(inputStream))));
	}
}
