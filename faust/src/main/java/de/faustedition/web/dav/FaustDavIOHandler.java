package de.faustedition.web.dav;

import java.io.IOException;
import java.util.Collection;

import javax.jcr.RepositoryException;

import org.apache.jackrabbit.server.io.DefaultHandler;
import org.apache.jackrabbit.server.io.ExportContext;
import org.apache.jackrabbit.server.io.ImportContext;

import com.google.common.collect.Lists;

import de.faustedition.util.ErrorUtil;

public class FaustDavIOHandler extends DefaultHandler {

	private Collection<FaustDavHandler> handlers = Lists.newArrayList();

	public FaustDavIOHandler(Collection<FaustDavHandler> handlers) {
		this.handlers = handlers;
	}

	@Override
	public boolean canImport(ImportContext context, boolean isCollection) {
		if (context == null || context.isCompleted()) {
			return false;
		}

		try {
			for (FaustDavHandler handler : handlers) {
				if (handler.canImport(context, isCollection)) {
					return true;
				}
			}
		} catch (RepositoryException e) {
			ErrorUtil.fatal(e);
		}
		return false;
	}

	@Override
	public boolean importContent(ImportContext context, boolean isCollection) throws IOException {
		try {
			for (FaustDavHandler handler : handlers) {
				if (handler.canImport(context, isCollection)) {
					return handler.importContent(context, isCollection);
				}
			}
		} catch (RepositoryException e) {
			ErrorUtil.fatal(e);
		}
		return false;
	}

	@Override
	public boolean canExport(ExportContext context, boolean isCollection) {
		if (context == null || context.isCompleted()) {
			return false;
		}

		try {
			for (FaustDavHandler handler : handlers) {
				if (handler.canExport(context, isCollection)) {
					return true;
				}
			}
		} catch (RepositoryException e) {
			ErrorUtil.fatal(e);
		}
		return false;
	}

	@Override
	public boolean exportContent(ExportContext context, boolean isCollection) throws IOException {
		try {
			for (FaustDavHandler handler : handlers) {
				if (handler.canExport(context, isCollection)) {
					return handler.exportContent(context, isCollection);
				}
			}
		} catch (RepositoryException e) {
			ErrorUtil.fatal(e);
		}
		return false;
	}
}
