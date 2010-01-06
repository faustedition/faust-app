package de.faustedition.model.facsimile;

import static de.faustedition.model.facsimile.FacsimileReference.NT_FACSIMILE;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.server.io.ExportContext;
import org.apache.jackrabbit.server.io.ImportContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.faustedition.util.ErrorUtil;
import de.faustedition.web.dav.AbstractDavHandler;

@Service
public class FacsimileReferenceDavHandler extends AbstractDavHandler {

	@Autowired
	private FacsimileImageDao facsimileImageDao;

	@Override
	public boolean canExport(ExportContext context, boolean isCollection) throws RepositoryException {
		return isOfNonCollectionNodeType(NT_FACSIMILE, context.getExportRoot(), isCollection);
	}

	@Override
	public boolean canImport(ImportContext context, boolean isCollection) throws RepositoryException {
		if (isCollection) {
			return false;
		}

		Item importRoot = context.getImportRoot();
		String name = context.getSystemId();
		if (name == null || importRoot == null || !importRoot.isNode()) {
			return false;
		}

		Node node = (Node) importRoot;
		if (!node.hasNode(name)) {
			return false;
		}

		return isOfNonCollectionNodeType(NT_FACSIMILE, node.getNode(name), false);
	}

	@Override
	public boolean exportContent(ExportContext context, boolean isCollection) throws RepositoryException {
		FacsimileReference reference = new FacsimileReference((Node) context.getExportRoot());
		String facsimileName = reference.getNode().getName();
		String facsimilePath = reference.getFacsimilePath();
		
		File facsimileFile = null;
		FacsimileImageResolution resolution = null;
		
		for (FacsimileImageResolution candidate : FacsimileImageResolution.values()) {
			if (candidate.matches(facsimileName)) {
				facsimileFile = facsimileImageDao.findImageFile(facsimilePath, candidate);
				if (facsimileFile != null) {
					resolution = candidate;
					break;
				}
			}
		}
		
		if (facsimileFile == null) {
			resolution = FacsimileImageResolution.LOW;
			facsimileFile = facsimileImageDao.findImageFile(facsimilePath, resolution);
			if (facsimileFile == null) {
				return false;
			}
		}
		
		FileInputStream imageDataStream = null;
		try {
			long contentLength = facsimileFile.length();
			long lastModified = facsimileFile.lastModified();
			if (context.hasStream()) {
				IOUtils.copy(imageDataStream = new FileInputStream(facsimileFile), context.getOutputStream());
			}
			context.setContentType(resolution.getMimeType(), null);
			context.setContentLength(contentLength);
			context.setModificationTime(lastModified);
			context.setETag(String.format("%s-%s", contentLength, lastModified));
			return true;
		} catch (IOException e) {
			ErrorUtil.fatal(e, "I/O error while writing facsimile '%s'", facsimileFile.getAbsolutePath());
		} finally {
			IOUtils.closeQuietly(imageDataStream);
		}

		return false;
	}

	@Override
	public boolean importContent(ImportContext context, boolean isCollection) throws RepositoryException {
		context.informCompleted(true);
		return true;
	}

}
