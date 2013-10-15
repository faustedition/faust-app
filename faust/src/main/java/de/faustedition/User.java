package de.faustedition;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class User implements Principal {


    public static final User SUPERUSER = new User("superuser", Sets.newHashSet("admin", "editor", "external"));
    public static final User ANONYMOUS = new User("anonymous", Collections.<String>emptySet());

    private final String name;
    private final Set<String> roles;

    public User(String name, Set<String> roles) {
        this.name = name;
        this.roles = roles;
    }

    @Override
    public String getName() {
        return name;
    }

    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof User) {
            return name.equals(((User) obj).name);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).addValue(name).addValue(Iterables.toString(roles)).toString();
    }

}
