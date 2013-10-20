package de.faustedition.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AbstractIdleService;
import de.faustedition.Database;
import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.db.Tables;
import de.faustedition.db.tables.records.ArchiveRecord;
import de.faustedition.xml.Sources;
import de.faustedition.xml.XMLUtil;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record2;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Singleton
public class Documents extends AbstractIdleService {
    public static final FaustURI DOCUMENT_BASE_URI = new FaustURI(FaustAuthority.XML, "/document");

    public static final FaustURI ARCHIVE_DESCRIPTOR_URI = new FaustURI(FaustAuthority.XML, "/archives.xml");

    private static final Logger LOG = Logger.getLogger(Documents.class.getName());

    private final Database database;
    private final Sources xml;
    private final ObjectMapper objectMapper;

    private final Map<String, Long> archives = Maps.newHashMap();

    @Inject
    public Documents(Database database, Sources xml, ObjectMapper objectMapper) {
        this.database = database;
        this.xml = xml;
        this.objectMapper = objectMapper;
    }

    public void update(final FaustURI source) {
        Preconditions.checkArgument(xml.isResource(source), source.toString());
        database.transaction(new Database.TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(DSLContext sql) throws Exception {
                update(source, sql);
                return null;
            }
        });
    }

    public void update(final FaustURI source, DSLContext sql) {
        Preconditions.checkArgument(xml.isResource(source), source.toString());
        final File descriptor = xml.file(source);
        final Record1<Timestamp> lastRead = sql.select(Tables.DOCUMENT.LAST_READ)
                .from(Tables.DOCUMENT)
                .where(Tables.DOCUMENT.DESCRIPTOR_URI.eq(uri2Path(source)))
                .fetchOne();
        if (lastRead == null || lastRead.value1().getTime() < descriptor.lastModified()) {
            try {
                XMLUtil.saxParser().parse(descriptor, new DocumentDescriptorParser(sql, source, objectMapper, xml, archives));
            } catch (SAXException e) {
                LOG.log(Level.SEVERE, "XML error while adding document " + source, e);
            } catch (IOException e) {
                LOG.log(Level.SEVERE, "I/O error while adding document " + source, e);
            }
        }
    }

    @Override
    protected void startUp() throws Exception {
        database.transaction(new Database.TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(final DSLContext sql) throws Exception {
                final Stopwatch sw = Stopwatch.createStarted();

                if (sql.selectCount().from(Tables.ARCHIVE).fetchOne().value1() == 0) {
                    XMLUtil.saxParser().parse(xml.file(ARCHIVE_DESCRIPTOR_URI), new ArchiveDescriptorParser(sql));
                }

                for (ArchiveRecord archiveRecord : sql.selectFrom(Tables.ARCHIVE).fetch()) {
                    archives.put(archiveRecord.getLabel(), archiveRecord.getId());
                }

                if (sql.selectCount().from(Tables.MATERIAL_UNIT).fetchOne().value1() == 0) {
                    final Map<String, Long> archiveIds = Maps.newHashMap();
                    for (Record2<Long, String> archive : sql.select(Tables.ARCHIVE.ID, Tables.ARCHIVE.LABEL).from(Tables.ARCHIVE).fetch()) {
                        archiveIds.put(archive.getValue(Tables.ARCHIVE.LABEL), archive.getValue(Tables.ARCHIVE.ID));
                    }
                    for (FaustURI documentDescriptor : xml.iterate(DOCUMENT_BASE_URI)) {
                        LOG.fine("<< " + documentDescriptor);
                        update(documentDescriptor, sql);
                    }
                }

                LOG.log(Level.INFO, "Built document scaffolding in {0}", sw.stop());

                return null;
            }
        });

    }

    @Override
    protected void shutDown() throws Exception {
    }

    static String uri2Path(FaustURI uri) {
        return uri.getPath().replaceAll("^/++", "");
    }

}
