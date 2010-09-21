package de.faustedition.security;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.restlet.data.ClientInfo;
import org.restlet.security.Enroler;
import org.restlet.security.Role;
import org.restlet.security.SecretVerifier;
import org.restlet.security.User;

import com.google.common.base.Joiner;
import com.google.inject.Inject;

public class LdapSecurityStore extends SecretVerifier implements Enroler {
    private static final String LDAP_SERVER_URL = "ldap://localhost/";

    private final Logger logger;
    private Map<String, UserData> cache = Collections.synchronizedMap(new HashMap<String, UserData>());

    @Inject
    public LdapSecurityStore(Logger logger) {
        super();
        this.logger = logger;
    }

    @Override
    public boolean verify(String identifier, char[] secret) throws IllegalArgumentException {
        if (identifier == null || identifier.length() == 0 || secret.length == 0) {
            return false;
        }

        UserData userData = cache.get(identifier);
        if (userData != null && compare(userData.secret, secret)) {
            logger.fine("Verifier found cached user data for " + identifier);
            return true;
        }

        final String userDn = String.format("uid=%s,ou=people,dc=faustedition,dc=uni-wuerzburg,dc=de", identifier);
        DirContext ctx = null;
        try {
            logger.fine("Verifier authenticates " + userDn);

            final Properties env = new Properties();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, LDAP_SERVER_URL);
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, userDn);
            env.put(Context.SECURITY_CREDENTIALS, secret);
            ctx = new InitialDirContext(env);

            final SearchControls searchCtrls = new SearchControls();
            searchCtrls.setReturningAttributes(new String[] { "cn" });
            searchCtrls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
            NamingEnumeration<SearchResult> answer = ctx.search("ou=groups,dc=faustedition,dc=uni-wuerzburg,dc=de",
                    "(&(uniqueMember=" + userDn + "))", searchCtrls);

            logger.fine("Verifier authenticated " + userDn + "; getting group memberships ...");
            final Set<Role> roles = new HashSet<Role>();
            while (answer.hasMore()) {
                String cn = (String) answer.next().getAttributes().get("cn").get();
                if ("admin".equals(cn)) {
                    roles.add(SecurityConstants.ADMIN_ROLE);
                    logger.fine("Giving role " + SecurityConstants.ADMIN_ROLE + " to " + identifier);
                } else if ("staff".equals(cn) || "editors".equals(cn)) {
                    roles.add(SecurityConstants.EDITOR_ROLE);
                    logger.fine("Giving role " + SecurityConstants.EDITOR_ROLE + " to " + identifier);
                }
            }

            cache.put(identifier, new UserData(secret, roles));
            return true;
        } catch (AuthenticationException e) {
            logger.log(Level.FINE, "Verifier failed authenticating " + userDn, e);
        } catch (NamingException e) {
            logger.log(Level.WARNING, "JNDI error while authenticating " + identifier + " against LDAP server", e);
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException e) {
                }
            }
        }

        return false;
    }

    @Override
    public void enrole(ClientInfo clientInfo) {
        User user = clientInfo.getUser();
        if (user == null) {
            return;
        }

        final String userName = user.getName();
        logger.fine("Enroler checks for roles of " + userName);
        UserData userData = cache.get(userName);
        if (userData == null) {
            logger.fine("Enroler did not find user " + userName);
            return;
        }

        logger.fine("Enroler assigns [" + Joiner.on(", ").join(userData.roles) + "] to user " + userName);
        clientInfo.getRoles().addAll(userData.roles);
    }

    private static class UserData {
        private final char[] secret;
        private final Set<Role> roles;

        private UserData(char[] secret, Set<Role> roles) {
            this.secret = secret;
            this.roles = roles;
        }
    }
}
