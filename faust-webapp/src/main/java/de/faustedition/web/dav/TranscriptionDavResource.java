package de.faustedition.web.dav;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.google.common.base.Preconditions;

import de.faustedition.model.manuscript.Facsimile;
import de.faustedition.model.manuscript.Manuscript;
import de.faustedition.model.manuscript.Transcription;
import de.faustedition.model.manuscript.TranscriptionDocument;

public class TranscriptionDavResource extends DavResourceBase implements GetableResource, PropFindableResource {

	private final Manuscript manuscript;
	private TranscriptionDocument transcriptionDocument;
	private byte[] transcriptionDocumentData;

	protected TranscriptionDavResource(DavResourceFactory factory, Manuscript manuscript) {
		super(factory);
		this.manuscript = manuscript;
	}

	@Override
	public String getName() {
		return manuscript.getPortfolio().getName() + "_" + manuscript.getName() + ".xml";
	}

	protected TranscriptionDocument getTranscriptionDocument() {
		if (transcriptionDocument == null) {
			transcriptionDocument = factory.getTranscriptionDocumentFactory().build(findTranscription());
		}
		return transcriptionDocument;

	}

	protected Transcription findTranscription() {
		Facsimile facsimile = Facsimile.find(factory.getDbSessionFactory().getCurrentSession(), manuscript, manuscript.getName());
		Preconditions.checkNotNull(facsimile);
		Transcription transcription = Transcription.find(factory.getDbSessionFactory().getCurrentSession(), facsimile);
		Preconditions.checkNotNull(transcription);
		return transcription;
	}

	public byte[] getTranscriptionDocumentData() {
		if (transcriptionDocumentData == null) {
			ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			getTranscriptionDocument().serialize(dataStream, true);
			transcriptionDocumentData = dataStream.toByteArray();
		}

		return transcriptionDocumentData;
	}

	@Override
	public Long getContentLength() {
		return Long.valueOf(getTranscriptionDocumentData().length);
	}

	@Override
	public String getContentType(String accepts) {
		return "application/xml";
	}

	@Override
	public Long getMaxAgeSeconds(Auth auth) {
		return null;
	}

	@Override
	public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException {
		if (transcriptionDocumentData == null) {
			getTranscriptionDocument().serialize(out, true);
		} else {
			IOUtils.write(getTranscriptionDocumentData(), out);
		}
		out.flush();
	}

	@Override
	public Object getLockResource() {
		return manuscript;
	}

	public void update(InputStream inputStream) throws IOException {
		factory.getTranscriptionDocumentFactory().parse(inputStream).update(findTranscription());
	}
}
