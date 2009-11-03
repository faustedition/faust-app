package de.faustedition.model;

import java.io.File;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;

import de.faustedition.util.ResourceUtil;

public class DataDirectoryFactoryBean implements FactoryBean<File>
{

	private Resource[] dataDirectoryResources;
	private File dataDirectory;

	@Required
	public void setDataDirectoryResources(Resource[] dataDirectoryResources)
	{
		this.dataDirectoryResources = dataDirectoryResources;
	}

	@Override
	public File getObject() throws Exception
	{
		if (dataDirectory == null)
		{
			Resource resource = ResourceUtil.chooseExistingResource(dataDirectoryResources);
			if (resource != null && resource.getFile() != null && resource.getFile().isDirectory())
			{
				dataDirectory = resource.getFile();
			}

			if (dataDirectory == null)
			{
				throw new IllegalStateException("Non of the data directories specified exists on this system.");
			}
		}

		return dataDirectory;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class getObjectType()
	{
		return File.class;
	}

	@Override
	public boolean isSingleton()
	{
		return true;
	}

}
