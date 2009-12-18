package de.faustedition.model.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesUserDetailsService;

@Configuration
public class SecurityConfiguration
{
	@Autowired
	private AuthenticationManager authenticationManager;

	@Bean
	public LocalNetPreAuthenticatedProcessingFilter localNetPreAuthenticatedProcessingFilter()
	{
		LocalNetPreAuthenticatedProcessingFilter processingFilter = new LocalNetPreAuthenticatedProcessingFilter();
		processingFilter.setAuthenticationManager(authenticationManager);
		processingFilter.setAuthenticationDetailsSource(new LocalNetPreAuthenticatedAuthenticationDetailsSource());
		return processingFilter;
	}

	@Bean
	public PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider()
	{
		PreAuthenticatedAuthenticationProvider authenticationProvider = new PreAuthenticatedAuthenticationProvider();
		authenticationProvider.setPreAuthenticatedUserDetailsService(new PreAuthenticatedGrantedAuthoritiesUserDetailsService());
		return authenticationProvider;
	}
}
