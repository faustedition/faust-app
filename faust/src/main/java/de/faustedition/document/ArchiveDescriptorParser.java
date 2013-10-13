package de.faustedition.document;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.db.Tables;
import de.faustedition.db.tables.records.ArchiveRecord;
import de.faustedition.xml.Namespaces;
import de.faustedition.xml.Sources;
import de.faustedition.xml.XMLUtil;
import org.jooq.DSLContext;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class ArchiveDescriptorParser extends DefaultHandler implements Runnable {

    public static final FaustURI ARCHIVE_DESCRIPTOR_URI = new FaustURI(FaustAuthority.XML, "/archives.xml");
    private static final Logger LOG = Logger.getLogger(ArchiveDescriptorParser.class.getName());

    private static final Set<String> PROPERTY_ELEMENT_NAMES = ImmutableSet.of(
            "name",
            "institution",
            "department",
            "city",
            "country",
            "url"
    );

    private final Sources sources;
    private final DSLContext sql;

    private ArchiveRecord archive;
    private StringBuilder textBuf;

    public ArchiveDescriptorParser(Sources sources, DSLContext sql) {
        this.sources = sources;
        this.sql = sql;
    }

    @Override
    public void run() {
        try {
            XMLUtil.saxParser().parse(sources.getInputSource(ArchiveDescriptorParser.ARCHIVE_DESCRIPTOR_URI), this);
        } catch (SAXException e) {
            throw Throwables.propagate(e);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
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
