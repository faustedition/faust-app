package de.faustedition.security;

import org.restlet.security.Role;

public interface AuthenticatorConstants {
    final Role EDITOR_ROLE = new Role("editor", "Member of the edition team");
    
    final Role ADMIN_ROLE = new Role("admin", "Administrative personel of the edition");
}
