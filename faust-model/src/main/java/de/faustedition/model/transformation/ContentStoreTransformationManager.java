package de.faustedition.model.transformation;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.model.repository.DataRepository;
import de.faustedition.util.ErrorUtil;
import de.faustedition.util.LoggingUtil;

public class ContentStoreTransformationManager {

	@Autowired
	private DataRepository dataRepository;

	private List<ContentTransformer> contentTransformers = new LinkedList<ContentTransformer>();

	public void setContentTransformers(List<ContentTransformer> contentTransformers) {
		this.contentTransformers = contentTransformers;
	}

	public void transform() {
		if (!contentTransformers.isEmpty()) {
			try {
				LoggingUtil.LOG.info("Transforming existing content");
				for (ContentTransformer transformer : contentTransformers) {
					LoggingUtil.LOG.info("Transforming existing content via " + new ToStringBuilder(transformer).toString());
					transformer.transformContent(dataRepository);
				}
			} catch (Exception e) {
				throw ErrorUtil.fatal("Error while transforming content repository", e);
			}
		}
	}
}
