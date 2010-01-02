package de.faustedition.model.dav;

import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.ServletRequest;

import de.faustedition.model.document.TranscriptionDocument;
import de.faustedition.model.hierarchy.HierarchyNode;
import de.faustedition.model.hierarchy.HierarchyNodeFacet;
import de.faustedition.model.tei.TEIDocumentManager;

@Service
public class DavResourceFactory implements ResourceFactory {
	private static final String DAV_SERVLET_PATH = "/dav";
	private static final LockTimeout DEFAULT_LOCK_TIMEOUT = new LockTimeout(1800L);
	private Map<Object, CurrentLock> locksByResource = new HashMap<Object, CurrentLock>();
	private Map<String, CurrentLock> locksByToken = new HashMap<String, CurrentLock>();

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private TEIDocumentManager teiDocumentManager;

	public SessionFactory getDbSessionFactory() {
		return sessionFactory;
	}

	public TEIDocumentManager getTeiDocumentManager() {
		return teiDocumentManager;
	}

	public Resource createResource(HierarchyNode node) {
		return new HierarchyNodeDavResource(this, node);
	}
	
	public Resource createResource(HierarchyNode node, String resourceName) {
		Session session = sessionFactory.getCurrentSession();

		if ((node.getName() + TranscriptionDavResource.RESOURCE_NAME_SUFFIX).equals(resourceName)) {
			TranscriptionDocument td = HierarchyNodeFacet.findByNode(session, node, TranscriptionDocument.class);
			return (td == null ? null : createResource(td));
		}

		return null;
	}

	public Resource createResource(HierarchyNodeFacet facet) {
		if (facet instanceof TranscriptionDocument) {
			return new TranscriptionDavResource(this, (TranscriptionDocument) facet);
		}

		return null;
	}

	@Override
	public Resource getResource(String host, String path) {
		path = StringUtils.strip(StringUtils.substringAfterLast(path, DAV_SERVLET_PATH), "/");
		Deque<String> pathComponents = HierarchyNode.getPathComponents(path);

		Session session = sessionFactory.getCurrentSession();
		HierarchyNode node = HierarchyNode.findByPath(session, pathComponents);
		if (node != null) {
			return createResource(node);
		}

		String resourceName = pathComponents.removeLast();
		node = HierarchyNode.findByPath(session, pathComponents);
		if (node != null) {
			return createResource(node, resourceName);
		}

		return null;
	}

	public String getBaseURI() {
		String absoluteUrl = HttpManager.request().getAbsoluteUrl();
		return StringUtils.strip(StringUtils.substringBeforeLast(absoluteUrl, DAV_SERVLET_PATH), "/");
	}

	@Override
	public String getSupportedLevels() {
		return "1,2";
	}

	public synchronized LockResult lock(LockTimeout timeout, LockInfo lockInfo, Object resource) {
		LockToken currentLock = currentLock(resource);
		if (currentLock != null) {
			return LockResult.failed(LockResult.FailureReason.ALREADY_LOCKED);
		}
		lockInfo.owner = ((ServletRequest) HttpManager.request()).getAuthorization().getUser();
		LockToken newToken = new LockToken(UUID.randomUUID().toString(), lockInfo, (timeout == null
				|| timeout.getSeconds() == null ? DEFAULT_LOCK_TIMEOUT : timeout));
		CurrentLock newLock = new CurrentLock(resource, newToken, lockInfo.owner);
		locksByResource.put(resource, newLock);
		locksByToken.put(newToken.tokenId, newLock);
		return LockResult.success(newToken);
	}

	public synchronized LockResult refresh(String tokenId, Object resource) {
		CurrentLock curLock = locksByResource.get(resource);
		curLock.token.setFrom(new Date());
		return LockResult.success(curLock.token);
	}

	public synchronized void unlock(String tokenId, Object resource) {
		LockToken lockToken = currentLock(resource);
		if (lockToken != null) {
			removeLock(lockToken);
		}
	}

	private LockToken currentLock(Object resource) {
		CurrentLock curLock = locksByResource.get(resource);
		if (curLock == null)
			return null;
		LockToken token = curLock.token;
		if (token.isExpired()) {
			removeLock(token);
			return null;
		} else {
			return token;
		}
	}

	private void removeLock(LockToken token) {
		CurrentLock currentLock = locksByToken.get(token.tokenId);
		locksByResource.remove(currentLock.resource);
		locksByToken.remove(currentLock.token.tokenId);
	}

	public LockToken getCurrentToken(Object resource) {
		CurrentLock lock = locksByResource.get(resource);
		if (lock == null)
			return null;
		LockToken token = new LockToken();
		token.info = new LockInfo(LockInfo.LockScope.EXCLUSIVE, LockInfo.LockType.WRITE, lock.owner,
				LockInfo.LockDepth.ZERO);
		token.timeout = lock.token.timeout;
		token.tokenId = lock.token.tokenId;
		return token;
	}

	private static class CurrentLock {
		final Object resource;
		final LockToken token;
		final String owner;

		private CurrentLock(Object resource, LockToken token, String owner) {
			this.resource = resource;
			this.token = token;
			this.owner = owner;
		}
	}
}
