package de.faustedition.document;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import de.faustedition.db.Tables;
import de.faustedition.db.tables.records.ArchiveRecord;
import de.faustedition.xml.Namespaces;
import org.jooq.DSLContext;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class ArchiveDescriptorParser extends DefaultHandler {

    private final Set<String> PROPERTY_ELEMENT_NAMES = ImmutableSet.of(
            "name",
            "institution",
            "department",
            "city",
            "country",
            "url"
    );

    private final DSLContext sql;

    private ArchiveRecord archive;
    private StringBuilder textBuf;

    public ArchiveDescriptorParser(DSLContext sql) {
        this.sql = sql;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (!Namespaces.FAUST_NS_URI.equals(uri)) {
            return;
        }
        if ("archive".equals(localName)) {
            archive = sql.newRecord(Tables.ARCHIVE);
            archive.setLabel(Preconditions.checkNotNull(attributes.getValue("id")));
        } else if (archive != null) {
            if (PROPERTY_ELEMENT_NAMES.contains(localName)) {
                textBuf = new StringBuilder();
            }
            if ("geolocation".equals(localName)) {
                archive.setLocationLat(Double.valueOf(attributes.getValue("lat")));
                archive.setLocationLng(Double.valueOf(attributes.getValue("lng")));
            } else if ("country".equals(localName)) {
                archive.setCountryCode(attributes.getValue("code"));
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (!Namespaces.FAUST_NS_URI.equals(uri)) {
            return;
        }
        if ("archive".equals(localName)) {
            archive.store();
            archive = null;
        } else if (PROPERTY_ELEMENT_NAMES.contains(localName) && textBuf != null) {
            final String text = textBuf.toString().trim();
            if ("name".equals(localName)) {
                archive.setName(text);
            } else if ("institution".equals(localName)) {
                archive.setInstitution(text);
            } else if ("department".equals(localName)) {
                archive.setDepartment(text);
            } else if ("city".equals(localName)) {
                archive.setCity(text);
            } else if ("country".equals(localName)) {
                archive.setCountry(text);
            } else if ("url".equals(localName)) {
                archive.setUrl(text);
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (textBuf != null) {
            textBuf.append(ch, start, length);
        }
    }

}
