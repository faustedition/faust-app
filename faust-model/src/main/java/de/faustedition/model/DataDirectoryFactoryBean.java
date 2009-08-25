package de.faustedition.model;

import java.io.File;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;

public class DataDirectoryFactoryBean implements FactoryBean {

	private Resource[] dataDirectoryResources;
	private File dataDirectory;
	
	@Required
	public void setDataDirectoryResources(Resource[] dataDirectoryResources) {
		this.dataDirectoryResources = dataDirectoryResources;
	}
	
	@Override
	public Object getObject() throws Exception {
		if (dataDirectory == null) {
			for (Resource dataDirectoryResource : dataDirectoryResources) {
				if (dataDirectoryResource.isReadable()) {
					File directoryCandidate = dataDirectoryResource.getFile();
					if (directoryCandidate != null && directoryCandidate.isDirectory()) {
						dataDirectory = directoryCandidate;
						break;
					}
				}
			}
			if (dataDirectory == null) {
				throw new IllegalStateException("Non of the data directories specified exists on this system.");
			}
		}
		
		return dataDirectory;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class getObjectType() {
		return File.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
