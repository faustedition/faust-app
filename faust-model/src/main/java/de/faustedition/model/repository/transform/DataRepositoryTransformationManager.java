package de.faustedition.model.repository.transform;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.model.repository.DataRepository;
import de.faustedition.util.ErrorUtil;
import de.faustedition.util.LoggingUtil;

public class DataRepositoryTransformationManager {

	@Autowired
	private DataRepository dataRepository;

	private List<RepositoryTransformer> repositoryTransformers = new LinkedList<RepositoryTransformer>();

	public void setRepositoryTransformers(List<RepositoryTransformer> repositoryTransformers) {
		this.repositoryTransformers = repositoryTransformers;
	}

	public void runTransformations() {
		if (!repositoryTransformers.isEmpty()) {
			try {
				for (RepositoryTransformer transformer : repositoryTransformers) {
					LoggingUtil.LOG.info("Transforming via " + new ToStringBuilder(transformer).toString());
					transformer.transformData(dataRepository);
				}
			} catch (Exception e) {
				throw ErrorUtil.fatal("Error while transforming data repository", e);
			}
		}
	}
}
