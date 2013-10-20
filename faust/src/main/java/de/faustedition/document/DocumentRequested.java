package de.faustedition.document;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class DocumentRequested {

    private final long id;

    public DocumentRequested(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
