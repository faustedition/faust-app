package de.faustedition;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.jersey.core.util.Base64;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.net.URI;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SecurityRequestFilter implements ContainerRequestFilter {
    private static final Logger LOG = Logger.getLogger(SecurityRequestFilter.class.getName());

    private static final String REALM = "faustedition.net";
    private static final String LDAP_SERVER_URL = "ldap://localhost/";

    private final Cache<String, User> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    private boolean authDisabled;

    @Inject
    public SecurityRequestFilter(@Named("auth.disable") boolean authDisabled) {
        this.authDisabled = authDisabled;
    }

    @Override
    public ContainerRequest filter(ContainerRequest request) {
        User user = null;

        final String authorization = request.getHeaderValue(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith("Basic ")) {
            final String userPassword = new String(Base64.decode(authorization.substring(6)));
            try {
                user = cache.get(userPassword, new Callable<User>() {

                    @Override
                    public User call() throws Exception {
                        final int userPasswordSeparatorIndex = userPassword.indexOf(':');
                        if ((userPasswordSeparatorIndex < 1) || (userPasswordSeparatorIndex >= (userPassword.length() - 1))) {
                            throw new AuthenticationException("Empty username or empty password in HTTP header");
                        }

                        final String user = userPassword.substring(0, userPasswordSeparatorIndex);
                        final String password = userPassword.substring(userPasswordSeparatorIndex + 1);

                        DirContext ctx = null;
                        try {
                            final String userDn = String.format("uid=%s,ou=people,dc=faustedition,dc=uni-wuerzburg,dc=de", user);

                            final Properties env = new Properties();
                            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
                            env.put(Context.PROVIDER_URL, LDAP_SERVER_URL);
                            env.put(Context.SECURITY_AUTHENTICATION, "simple");
                            env.put(Context.SECURITY_PRINCIPAL, userDn);
                            env.put(Context.SECURITY_CREDENTIALS, password);
                            ctx = new InitialDirContext(env);

                            final SearchControls searchCtrls = new SearchControls();
                            searchCtrls.setReturningAttributes(new String[] { "cn" });
                            searchCtrls.setSearchScope(SearchControls.ONELEVEL_SCOPE);

                            final Set<String> roles = new HashSet<String>();

                            final NamingEnumeration<SearchResult> answer = ctx.search(
                                    "ou=groups,dc=faustedition,dc=uni-wuerzburg,dc=de",
                                    "(&(uniqueMember=" + userDn + "))",
                                    searchCtrls
                            );

                            try {
                                while (answer.hasMore()) {
                                    roles.add((String) answer.next().getAttributes().get("cn").get());
                                }
                            } finally {
                                answer.close();
                            }

                            return new User(user, roles);
                        } finally {
                            if (ctx != null) {
                                try {
                                    ctx.close();
                                } catch (NamingException e) {
                                }
                            }
                        }

                    }
                });
            } catch (ExecutionException e) {
                final Throwable cause = e.getCause();
                if (cause instanceof AuthenticationException) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.SEVERE, "Authentication failed", cause);
                    }
                } else {
                    if (LOG.isLoggable(Level.SEVERE)) {
                        LOG.log(Level.SEVERE, "Error while accessing LDAP server", cause);
                    }
                }
            }
        }

        final URI absolutePath = request.getAbsolutePath();
        final String requestPath = request.getPath();

        if (user == null && authDisabled) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Authentication disabled! Superuser access to resource {0}", absolutePath);
            }
            user = User.SUPERUSER;
        }

        if (user == null && isPublicPath(requestPath)) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Anonymous access to public resource {0}", absolutePath);
            }
            user = User.ANONYMOUS;
        }

        if (user == null && "OPTIONS".equals(request.getMethod())) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Anonymous access to resource {0} accessed via OPTIONS method", absolutePath);
            }
            user = User.ANONYMOUS;
        }

        if (user == null) {
            throw new WebApplicationException(Response
                    .status(Response.Status.UNAUTHORIZED)
                    .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"" + SecurityRequestFilter.REALM + "\"")
                    .build());
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Authorized access to resource {0} by {1}", new Object[] {absolutePath, user });
        }

        final User contextPrincipal = user;
        request.setSecurityContext(new SecurityContext() {
            @Override
            public java.security.Principal getUserPrincipal() {
                return contextPrincipal;
            }

            @Override
            public boolean isUserInRole(String role) {
                return contextPrincipal.getRoles().contains(role);
            }

            @Override
            public boolean isSecure() {
                return false;
            }

            @Override
            public String getAuthenticationScheme() {
                return BASIC_AUTH;
            }
        });

        return request;
    }

    protected static boolean isPublicPath(String requestPath) {
        return requestPath.isEmpty() || requestPath.startsWith("resources") || requestPath.startsWith("static") || requestPath.startsWith("project");
    }
}
