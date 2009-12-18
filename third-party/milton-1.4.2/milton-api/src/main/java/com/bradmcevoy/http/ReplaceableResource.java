package com.bradmcevoy.http;

import java.io.InputStream;

/**
 * Indicates a resource which can have its content replaced by a PUT method
 *
 * @author brad
 */
public interface ReplaceableResource extends Resource {

    public void replaceContent(InputStream in, Long length);

}
