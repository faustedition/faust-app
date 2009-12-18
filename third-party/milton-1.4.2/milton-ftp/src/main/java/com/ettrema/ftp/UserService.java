package com.ettrema.ftp;

/**
 *
 * @author brad
 */
public interface UserService {

    /**
     * don't need it
     *
     * @param name
     */
    public void delete( String name );

    /**
     * * don't need it
     *
     * @param name
     * @return
     */
    public boolean doesExist( String name );

    /**
     * * don't need it
     *
     * @return
     */
    public String[] getAllUserNames();

    /**
     *
     * @param name - milton form of the username Eg user@authority
     * @param domain - the domain to login into
     * @return
     */
    public MiltonUser getUserByName( String name, String domain );

    /**
     * Save the user. You don't need to implement this
     * @param user
     */
    public void save( MiltonUser user );

}
