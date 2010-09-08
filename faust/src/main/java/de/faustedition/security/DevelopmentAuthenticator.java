package de.faustedition.security;

import java.util.List;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ClientInfo;
import org.restlet.security.Authenticator;
import org.restlet.security.Enroler;
import org.restlet.security.Role;

public class DevelopmentAuthenticator extends Authenticator {

    public DevelopmentAuthenticator(Context context) {
        super(context);
        setEnroler(ALL_ROLES_ENROLER);
    }

    @Override
    protected boolean authenticate(Request request, Response response) {
        return true;
    }

    private static final Enroler ALL_ROLES_ENROLER = new Enroler() {

        @Override
        public void enrole(ClientInfo clientInfo) {
            List<Role> roles = clientInfo.getRoles();
            roles.add(AuthenticatorConstants.EDITOR_ROLE);
            roles.add(AuthenticatorConstants.ADMIN_ROLE);
        }
    };
}
