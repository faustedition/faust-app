package de.faustedition.web.dav;

import java.util.Date;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.LockableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Request.Method;

public abstract class DavResourceBase implements Resource, GetableResource, PropFindableResource, LockableResource {

	protected final DavResourceFactory factory;

	protected DavResourceBase(DavResourceFactory factory) {
		this.factory = factory;
	}

	@Override
	public Object authenticate(String user, String password) {
		return "";
	}

	@Override
	public boolean authorise(Request request, Method method, Auth auth) {
		return true;
	}

	@Override
	public String checkRedirect(Request request) {
		return null;
	}

	@Override
	public Date getCreateDate() {
		return new Date(0);
	}

	@Override
	public Date getModifiedDate() {
		return new Date(0);
	}

	@Override
	public String getRealm() {
		return "realm";
	}

	@Override
	public String getUniqueId() {
		return null;
	}
	
	public abstract Object getLockResource();
	
	
	@Override
	public LockResult lock(LockTimeout timeout, LockInfo lockInfo) {
		return factory.lock(timeout, lockInfo, getLockResource());
	}
	
	@Override
	public LockResult refreshLock(String token) {
		return factory.refresh(token, getLockResource());
	}
	
	@Override
	public void unlock(String tokenId) {
		factory.unlock(tokenId, getLockResource());
	}
	
	@Override
	public LockToken getCurrentLock() {
		return factory.getCurrentToken(getLockResource());
	}
}
