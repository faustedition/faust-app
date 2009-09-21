package de.faustedition.web.security;

import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.ui.AuthenticationDetailsSourceImpl;
import org.springframework.security.ui.preauth.PreAuthenticatedGrantedAuthoritiesAuthenticationDetails;

public class LocalNetPreAuthenticatedAuthenticationDetailsSource extends AuthenticationDetailsSourceImpl
{

	public LocalNetPreAuthenticatedAuthenticationDetailsSource()
	{
		super();
		setClazz(PreAuthenticatedGrantedAuthoritiesAuthenticationDetails.class);
	}

	@Override
	public Object buildDetails(Object context)
	{
		PreAuthenticatedGrantedAuthoritiesAuthenticationDetails details = (PreAuthenticatedGrantedAuthoritiesAuthenticationDetails) super.buildDetails(context);
		details.setGrantedAuthorities(new GrantedAuthority[] { new GrantedAuthorityImpl("ROLE_EDITOR") });
		return details;
	}
}
