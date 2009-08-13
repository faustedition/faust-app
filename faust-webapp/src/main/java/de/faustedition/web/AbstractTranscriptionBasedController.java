package de.faustedition.web;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import de.faustedition.model.store.ContentStore;
import de.faustedition.model.transcription.TranscriptionStore;
import de.faustedition.util.ErrorUtil;

public abstract class AbstractTranscriptionBasedController {
	@Autowired
	protected ContentStore contentStore;

	private TranscriptionStore transcriptionStore;

	public static String getPath(HttpServletRequest request) {
		return StringUtils.strip(StringUtils.defaultString(request.getPathInfo()), "/");
	}

	protected TranscriptionStore getTranscriptionStore() {
		if (transcriptionStore == null) {
			try {
				transcriptionStore = contentStore.findTranscriptionStore();
				Assert.notNull(transcriptionStore, "Cannot find transcription store");
			} catch (RepositoryException e) {
				throw ErrorUtil.fatal("Error looking up transcription store", e);
			}

		}
		return transcriptionStore;
	}

}
