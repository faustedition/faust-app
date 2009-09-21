package de.faustedition.web.dav;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.ServletRequest;
import com.google.common.base.Function;

import de.faustedition.model.manuscript.Manuscript;
import de.faustedition.model.manuscript.Portfolio;
import de.faustedition.model.manuscript.Repository;
import de.faustedition.model.manuscript.TranscriptionDocumentFactory;

public class DavResourceFactory implements ResourceFactory
{
	private static final String DAV_SERVLET_PATH = "/dav";
	private static final LockTimeout DEFAULT_LOCK_TIMEOUT = new LockTimeout(1800L);

	private Map<Object, CurrentLock> locksByResource = new HashMap<Object, CurrentLock>();
	private Map<String, CurrentLock> locksByToken = new HashMap<String, CurrentLock>();

	@Autowired
	private SessionFactory dbSessionFactory;

	public SessionFactory getDbSessionFactory()
	{
		return dbSessionFactory;
	}

	@Override
	public Resource getResource(String host, String path)
	{
		Resource resource = new RootDavResource(this);
		for (String part : Path.path(StringUtils.strip(StringUtils.substringAfterLast(path, DAV_SERVLET_PATH), "/")).getParts())
		{
			if ((resource == null) || !(resource instanceof CollectionResource))
			{
				return null;
			}
			resource = ((CollectionResource) resource).child(part);
		}
		return resource;
	}

	public TranscriptionDocumentFactory getTranscriptionDocumentFactory()
	{
		String baseUri = StringUtils.strip(StringUtils.substringBeforeLast(HttpManager.request().getAbsoluteUrl(), DAV_SERVLET_PATH), "/");
		return new TranscriptionDocumentFactory(baseUri + "/schema/v1/faust-tei.rnc", baseUri + "/schema/v1/faust-tei.css");
	}

	@Override
	public String getSupportedLevels()
	{
		return "1,2";
	}

	public synchronized LockResult lock(LockTimeout timeout, LockInfo lockInfo, Object resource)
	{
		LockToken currentLock = currentLock(resource);
		if (currentLock != null)
		{
			return LockResult.failed(LockResult.FailureReason.ALREADY_LOCKED);
		}
		lockInfo.owner = ((ServletRequest) HttpManager.request()).getAuthorization().getUser();
		LockToken newToken = new LockToken(UUID.randomUUID().toString(), lockInfo, (timeout == null || timeout.getSeconds() == null ? DEFAULT_LOCK_TIMEOUT : timeout));
		CurrentLock newLock = new CurrentLock(resource, newToken, lockInfo.owner);
		locksByResource.put(resource, newLock);
		locksByToken.put(newToken.tokenId, newLock);
		return LockResult.success(newToken);
	}

	public synchronized LockResult refresh(String tokenId, Object resource)
	{
		CurrentLock curLock = locksByResource.get(resource);
		curLock.token.setFrom(new Date());
		return LockResult.success(curLock.token);
	}

	public synchronized void unlock(String tokenId, Object resource)
	{
		LockToken lockToken = currentLock(resource);
		if (lockToken != null)
		{
			removeLock(lockToken);
		}
	}

	private LockToken currentLock(Object resource)
	{
		CurrentLock curLock = locksByResource.get(resource);
		if (curLock == null)
			return null;
		LockToken token = curLock.token;
		if (token.isExpired())
		{
			removeLock(token);
			return null;
		} else
		{
			return token;
		}
	}

	private void removeLock(LockToken token)
	{
		CurrentLock currentLock = locksByToken.get(token.tokenId);
		locksByResource.remove(currentLock.resource);
		locksByToken.remove(currentLock.token.tokenId);
	}

	public LockToken getCurrentToken(Object resource)
	{
		CurrentLock lock = locksByResource.get(resource);
		if (lock == null)
			return null;
		LockToken token = new LockToken();
		token.info = new LockInfo(LockInfo.LockScope.EXCLUSIVE, LockInfo.LockType.WRITE, lock.owner, LockInfo.LockDepth.ZERO);
		token.timeout = lock.token.timeout;
		token.tokenId = lock.token.tokenId;
		return token;
	}

	private static class CurrentLock
	{
		final Object resource;
		final LockToken token;
		final String owner;

		private CurrentLock(Object resource, LockToken token, String owner)
		{
			this.resource = resource;
			this.token = token;
			this.owner = owner;
		}
	}

	protected final Function<Manuscript, TranscriptionDavResource> transcriptionResourceCreator = new Function<Manuscript, TranscriptionDavResource>()
	{

		@Override
		public TranscriptionDavResource apply(Manuscript from)
		{
			return new TranscriptionDavResource(DavResourceFactory.this, from);
		}
	};
	protected Function<Portfolio, PortfolioDavResource> portfolioResourceCreator = new Function<Portfolio, PortfolioDavResource>()
	{

		@Override
		public PortfolioDavResource apply(Portfolio from)
		{
			return new PortfolioDavResource(DavResourceFactory.this, from);
		}
	};
	protected Function<Repository, RepositoryDavResource> repositoryResourceCreator = new Function<Repository, RepositoryDavResource>()
	{

		@Override
		public RepositoryDavResource apply(Repository from)
		{
			return new RepositoryDavResource(DavResourceFactory.this, from);
		}
	};
}
