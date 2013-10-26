package de.faustedition.text;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextContent implements TextToken {

    private final String content;

    public TextContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return ("'" + getContent().replaceAll("[\r\n]+", "\u00b6") + "'");
    }
}
