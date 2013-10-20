package de.faustedition.document;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
* @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
*/
class FacsimileReferenceParser extends DefaultHandler {

    private static final Logger LOG = Logger.getLogger(FacsimileReferenceParser.class.getName());

    private final FaustURI source;

    private List<String> facsimileReferences = Lists.newLinkedList();
    private String textImageLinkReference = null;

    FacsimileReferenceParser(FaustURI source) {
        this.source = source;
    }

    List<String> getFacsimileReferences() {
        return facsimileReferences;
    }

    String getTextImageLinkReference() {
        return textImageLinkReference;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (!localName.equals("graphic")) {
            return;
        }
        final String url = Strings.nullToEmpty(attributes.getValue("url")).trim();
        final boolean isFacsimile = Strings.nullToEmpty(attributes.getValue("mimeType")).trim().isEmpty();
        if (url.isEmpty()) {
            return;
        }
        try {
            final FaustURI referenceUri = new FaustURI(URI.create(url));
            final String path = Documents.uri2Path(referenceUri);
            if (isFacsimile) {
                Preconditions.checkArgument(referenceUri.getAuthority() == FaustAuthority.FACSIMILE);
                facsimileReferences.add(path);
            } else if (textImageLinkReference == null) {
                Preconditions.checkArgument(referenceUri.getAuthority() == FaustAuthority.XML);
                textImageLinkReference = path;
            }
        } catch (IllegalArgumentException e) {
            if (LOG.isLoggable(Level.WARNING)) {
                LOG.warning("Invalid URI '" + url + "' in " + source);
            }
        }
    }
}
