package de.faustedition.web.dav;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import de.faustedition.web.ControllerUtil;

public abstract class AbstractDavHandler implements FaustDavHandler {

	protected String getBaseURI() {
		return ControllerUtil.getBaseURI(getServletRequestAttributes().getRequest());
	}

	protected ServletRequestAttributes getServletRequestAttributes() {
		return (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
	}
	
	protected boolean isOfNonCollectionNodeType(String type, Item item, boolean isCollection) throws RepositoryException {
		if (isCollection) {
			return false;
		}
		return item != null && item.isNode() && ((Node) item).isNodeType(type);
	}
}
