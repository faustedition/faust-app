package de.faustedition.db;

import de.faustedition.Configuration;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Console {

    private static final Logger LOG = Logger.getLogger(Console.class.getName());

    public static void main(String... args) {
        try {
            Relations.console(new File(Configuration.read().getProperty("db.home")));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
