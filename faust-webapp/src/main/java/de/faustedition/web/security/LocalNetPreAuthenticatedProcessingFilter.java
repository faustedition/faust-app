package de.faustedition.web.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.ui.FilterChainOrder;
import org.springframework.security.ui.preauth.AbstractPreAuthenticatedProcessingFilter;

public class LocalNetPreAuthenticatedProcessingFilter extends AbstractPreAuthenticatedProcessingFilter {
	private static final String LOCAL_NET_TOKEN = "localnet";
	private static final String[] LOCAL_NET_ADDRESS_PREFIXES = new String[] { "127.0.0.1", "0:0:0:0:0:0:0:1", "192.168.1.", "192.168.2." };

	@Override
	protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
		return isRequestFromLocalNet(request) ? LOCAL_NET_TOKEN : null;
	}

	@Override
	protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
		return isRequestFromLocalNet(request) ? LOCAL_NET_TOKEN : null;
	}

	private static boolean isRequestFromLocalNet(HttpServletRequest request) {
		for (String addressPrefix : LOCAL_NET_ADDRESS_PREFIXES) {
			if (request.getRemoteHost().startsWith(addressPrefix)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public int getOrder() {
		return FilterChainOrder.PRE_AUTH_FILTER;
	}

}
