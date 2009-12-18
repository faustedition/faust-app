package com.ettrema.console;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;

/**
 *
 * @author brad
 */
public interface LinkGenerator {
    public String link(Path parentPath, Resource r) ;
}
