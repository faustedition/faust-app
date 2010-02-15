package de.faustedition.security;

import java.util.Arrays;

import org.springframework.security.authentication.AuthenticationDetailsSourceImpl;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesAuthenticationDetails;

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
		details.setGrantedAuthorities(Arrays.asList(new GrantedAuthority[] { new GrantedAuthorityImpl("ROLE_EDITOR") }));
		return details;
	}
}
