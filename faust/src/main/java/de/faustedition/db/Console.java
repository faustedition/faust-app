package de.faustedition.db;

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
            Relations.console(new File(args.length > 0 ? args[0] : "data"));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
