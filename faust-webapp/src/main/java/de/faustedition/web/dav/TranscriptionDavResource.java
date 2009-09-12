package de.faustedition.web.dav;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.google.common.base.Preconditions;

import de.faustedition.model.manuscript.Facsimile;
import de.faustedition.model.manuscript.Manuscript;
import de.faustedition.model.manuscript.Transcription;
import de.faustedition.model.manuscript.TranscriptionType;
import de.faustedition.util.ErrorUtil;

public class TranscriptionDavResource extends DavResourceBase implements GetableResource {

	private final Manuscript manuscript;
	private byte[] transcriptionData;

	protected TranscriptionDavResource(DavResourceFactory factory, Manuscript manuscript) {
		super(factory);
		this.manuscript = manuscript;
	}

	@Override
	public String getName() {
		return manuscript.getPortfolio().getName() + "_" + manuscript.getName();
	}

	protected byte[] getTranscriptionData() {
		if (transcriptionData == null) {
			for (Facsimile f : Facsimile.find(factory.getDbSessionFactory().getCurrentSession(), manuscript)) {
				if (!f.getName().equals(manuscript.getName())) {
					continue;
				}
				Transcription transcription = Transcription.find(factory.getDbSessionFactory().getCurrentSession(), f, TranscriptionType.DOCUMENT_AND_TEXT);
				if (transcription == null) {
					break;
				}
				try {
					transcriptionData = factory.getTranscriptionDocumentFactory().build(transcription).serialize();
				} catch (IOException e) {
					throw ErrorUtil.fatal("I/O error while generating transcription document", e);
				}
			}
		}
		Preconditions.checkNotNull(transcriptionData);
		return transcriptionData;
	}

	@Override
	public Long getContentLength() {
		return Long.valueOf(getTranscriptionData().length);
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
		IOUtils.write(getTranscriptionData(), out);
	}
}
