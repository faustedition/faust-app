package de.faustedition.web.dav;

import java.util.Date;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Request.Method;

public abstract class DavResourceBase implements Resource {

	protected final DavResourceFactory factory;

	protected DavResourceBase(DavResourceFactory factory) {
		this.factory = factory;
	}

	@Override
	public Object authenticate(String user, String password) {
		return null;
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
	public Date getModifiedDate() {
		return new Date();
	}

	@Override
	public String getRealm() {
		return "realm";
	}

	@Override
	public String getUniqueId() {
		return null;
	}
}
