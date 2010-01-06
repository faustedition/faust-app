package de.faustedition.web.dav;

import javax.jcr.RepositoryException;

import org.apache.jackrabbit.server.io.ExportContext;
import org.apache.jackrabbit.server.io.ImportContext;

public interface FaustDavHandler {
	boolean canImport(ImportContext context, boolean isCollection) throws RepositoryException;

	boolean importContent(ImportContext context, boolean isCollection) throws RepositoryException;

	boolean canExport(ExportContext context, boolean isCollection) throws RepositoryException;

	boolean exportContent(ExportContext context, boolean isCollection) throws RepositoryException;
}
