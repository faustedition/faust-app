package de.faustedition.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Objects;
import com.google.common.collect.Range;
import com.google.common.eventbus.EventBus;
import de.faustedition.Database;
import de.faustedition.Templates;
import de.faustedition.db.Tables;
import de.faustedition.db.tables.records.ArchiveRecord;
import de.faustedition.db.tables.records.DocumentTranscriptAlignmentRecord;
import de.faustedition.transcript.Transcript;
import de.faustedition.transcript.Transcripts;
import de.faustedition.transcript.VerseIndex;
import de.faustedition.xml.Sources;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Result;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.List;
import java.util.Map;


@Path("/document")
@Singleton
public class DocumentResource {

    private final Database database;
    private final EventBus eventBus;
    private final Documents documents;
    private final Transcripts transcripts;
    private final VerseIndex verseIndex;
    private final Sources sources;
    private final Templates templates;
    private final ObjectMapper objectMapper;

    @Inject
    public DocumentResource(Database database, EventBus eventBus, Documents documents, Transcripts transcripts, VerseIndex verseIndex, Sources sources, Templates templates, ObjectMapper objectMapper) {
        this.database = database;
        this.eventBus = eventBus;
        this.documents = documents;
        this.transcripts = transcripts;
        this.verseIndex = verseIndex;
        this.sources = sources;
        this.templates = templates;
        this.objectMapper = objectMapper;
    }

    @GET
    public Response indexView(@Context final Request request) {
        return database.transaction(new Database.TransactionCallback<Response>() {
            @Override
            public Response doInTransaction(DSLContext sql) throws Exception {
                return templates.render(request, new Templates.ViewAndModel("document/archives").add("archives", index()));
            }
        });
    }

    @GET
    @Path("/data/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, Object>> index() {
        return database.transaction(new Database.TransactionCallback<List<Map<String, Object>>>() {
            @Override
            public List<Map<String, Object>> doInTransaction(DSLContext sql) throws Exception {
                return sql.selectFrom(Tables.ARCHIVE).orderBy(Tables.ARCHIVE.NAME.asc()).fetchMaps();
            }
        });
    }

    @Path("/archive/{id}/")
    @GET
    public Response archiveView(@PathParam("id") final String id, @Context final Request request) {
        return templates.render(request, archive(id));
    }

    @GET
    @Path("/archive/{id}/data/")
    @Produces(MediaType.APPLICATION_JSON)
    public Templates.ViewAndModel archive(@PathParam("id") final String id) {
        return database.transaction(new Database.TransactionCallback<Templates.ViewAndModel>() {
            @Override
            public Templates.ViewAndModel doInTransaction(DSLContext sql) throws Exception {
                final ArchiveRecord archive = documents.getArchivesByLabel().get(id);
                if (archive == null) {
                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(id).build());
                }

                return new Templates.ViewAndModel("document/archive")
                        .add("archive", archive.intoMap())
                        .add("documents", sql
                                .select(Tables.DOCUMENT.ID, Tables.DOCUMENT.CALLNUMBER, Tables.DOCUMENT.WA_ID)
                                .from(Tables.DOCUMENT)
                                .where(Tables.DOCUMENT.ARCHIVE_ID.eq(archive.getId()))
                                .orderBy(Tables.DOCUMENT.CALLNUMBER.asc())
                                .fetchMaps()
                        );
            }

            @Override
            protected boolean rollsBackOn(Exception e) {
                return !(e instanceof WebApplicationException);
            }
        });
    }

    @Path("/verse/{num}/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ObjectNode verseConcordance(@PathParam("num") final int verse) throws Exception {
        final ObjectNode result = objectMapper.createObjectNode();
        for (Map.Entry<Long, Range<Integer>> document : verseIndex.query(verse, 100).entrySet()) {
            final Range<Integer> verseSegment = document.getValue();
            result.putArray(Long.toString(document.getKey()))
                    .add(verseSegment.lowerEndpoint())
                    .add(verseSegment.upperEndpoint());
        }
        return result;
    }

    @GET
    @Path("/{id}/")
    public Response documentView(@Context Request request, @PathParam("id") final long id) {
        return templates.render(request, document(id));
    }

    @GET
    @Path("/{id}/collation/")
    @Produces(MediaType.APPLICATION_JSON)
    public ObjectNode collation(@PathParam("id") final long id, @QueryParam("compressed") @DefaultValue("false") final boolean compressed) {
        return database.transaction(new Database.TransactionCallback<ObjectNode>() {
            @Override
            public ObjectNode doInTransaction(DSLContext sql) throws Exception {
                return collation(sql, id, compressed);
            }
        });
    }

    protected ObjectNode collation(DSLContext sql, final long id, final boolean compressed) {
        final Result<DocumentTranscriptAlignmentRecord> aligmentRecords = sql.selectFrom(Tables.DOCUMENT_TRANSCRIPT_ALIGNMENT)
                .where(Tables.DOCUMENT_TRANSCRIPT_ALIGNMENT.DOCUMENT_ID.eq(id))
                .orderBy(Tables.DOCUMENT_TRANSCRIPT_ALIGNMENT.DOC_START.asc(), Tables.DOCUMENT_TRANSCRIPT_ALIGNMENT.DOC_END.desc())
                .fetch();
        final ObjectNode collation = objectMapper.createObjectNode();
        final ArrayNode alignments = collation.putArray("alignments");
        final ArrayNode transpositions = collation.putArray("transpositions");

        int docStart = -1;
        int docEnd = -1;
        int textStart = -1;
        int textEnd = -1;
        boolean changed = false;
        for (DocumentTranscriptAlignmentRecord alignment : aligmentRecords) {
            if (alignment.getIsTransposition()) {
                // transpositions are never compressed
                transpositions.addArray()
                        .add(alignment.getDocStart()).add(alignment.getDocEnd())
                        .add(alignment.getTextStart()).add(alignment.getTextEnd());
                continue;
            } else if (!compressed) {
                // in uncompressed mode just emit each alignment
                final ArrayNode alignmentDesc = alignments.addArray()
                        .add(alignment.getDocStart()).add(alignment.getDocEnd())
                        .add(alignment.getTextStart()).add(alignment.getTextEnd());

                if (alignment.getIsChanged()) {
                    alignmentDesc.add(false);
                }
                continue;
            }
            if (alignment.getDocStart() == docEnd && alignment.getTextStart() == textEnd && alignment.getIsChanged().equals(changed)) {
                // extend alignments in compressed mode
                docEnd = alignment.getDocEnd();
                textEnd = alignment.getTextEnd();
                continue;
            }
            if (docStart >= 0 && textStart >= 0) {
                final ArrayNode alignmentDesc = alignments.addArray()
                        .add(docStart).add(docEnd)
                        .add(textStart).add(textEnd);

                if (changed) {
                    alignmentDesc.add(false);
                }
            }
            docStart = alignment.getDocStart();
            docEnd = alignment.getDocEnd();
            textStart = alignment.getTextStart();
            textEnd = alignment.getTextEnd();
            changed = alignment.getIsChanged();
        }
        if (docStart >= 0 && textStart >= 0) {
            final ArrayNode alignmentDesc = alignments.addArray()
                    .add(docStart).add(docEnd)
                    .add(textStart).add(textEnd);

            if (changed) {
                alignmentDesc.add(false);
            }
        }

        return collation;
    }

    @GET
    @Path("/{id}/data/")
    @Produces(MediaType.APPLICATION_JSON)
    public Templates.ViewAndModel document(@PathParam("id") final long id) {
        return database.transaction(new Database.TransactionCallback<Templates.ViewAndModel>() {
            @Override
            public Templates.ViewAndModel doInTransaction(DSLContext sql) throws Exception {
                final Record3<Long, String, String> document = sql
                        .select(Tables.DOCUMENT.ARCHIVE_ID, Tables.DOCUMENT.CALLNUMBER, Tables.DOCUMENT.METADATA)
                        .from(Tables.DOCUMENT)
                        .where(Tables.DOCUMENT.ID.eq(id))
                        .fetchOne();

                if (document == null) {
                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(id).build());
                }

                eventBus.post(new DocumentRequested(id));

                final Result<Record3<Long, Long, String>> materialUnits = sql
                        .select(Tables.MATERIAL_UNIT.ID, Tables.TRANSCRIPT.ID, Tables.FACSIMILE.PATH)
                        .from(Tables.MATERIAL_UNIT)
                        .leftOuterJoin(Tables.TRANSCRIPT).on(Tables.TRANSCRIPT.MATERIAL_UNIT_ID.eq(Tables.MATERIAL_UNIT.ID))
                        .leftOuterJoin(Tables.FACSIMILE).on(Tables.FACSIMILE.TRANSCRIPT_ID.eq(Tables.TRANSCRIPT.ID))
                        .where(Tables.MATERIAL_UNIT.DOCUMENT_ID.eq(id))
                        .orderBy(Tables.MATERIAL_UNIT.DOCUMENT_ORDER.asc(), Tables.FACSIMILE.FACSIMILE_ORDER.asc())
                        .fetch();

                final ArrayNode references = objectMapper.createArrayNode();
                ArrayNode facsimiles = null;
                long currentUnit = 0;
                for (Record3<Long, Long, String> materialUnitData : materialUnits) {
                    if (materialUnitData.value1() != currentUnit) {
                        currentUnit = materialUnitData.value1();
                        facsimiles = references.addObject().put("transcript", materialUnitData.value2()).putArray("facsimiles");
                    }
                    if (materialUnitData.value3() != null) {
                        facsimiles.add(materialUnitData.value3());
                    }
                }

                final Transcript textualTranscript = transcripts.textual(id);
                return new Templates.ViewAndModel("document")
                        .add("id", id)
                        .add("archive", documents.getArchivesById().get(Objects.firstNonNull(document.value1(), 0L)))
                        .add("callnumber", document.value2())
                        .add("metadata", objectMapper.reader().readTree(document.value3()))
                        .add("references", references)
                        .add("text", textualTranscript)
                        .add("documentaryTranscript", transcripts.documentary(id).json())
                        .add("textualTranscript", textualTranscript.json())
                        .add("collation", collation(sql, id, false));
            }

            @Override
            protected boolean rollsBackOn(Exception e) {
                return !(e instanceof WebApplicationException);
            }
        });
    }

    @GET
    @Path("/{id}/source/")
    @Produces(MediaType.APPLICATION_XML)
    public File documentDescriptor(@PathParam("id") final long id) {
        return database.transaction(new Database.TransactionCallback<File>() {
            @Override
            public File doInTransaction(DSLContext sql) throws Exception {
                final Record1<String> descriptor = sql
                        .select(Tables.DOCUMENT.DESCRIPTOR_PATH)
                        .from(Tables.DOCUMENT)
                        .where(Tables.DOCUMENT.ID.eq(id))
                        .fetchOne();

                if (descriptor == null) {
                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(id).build());
                }

                final File descriptorFile = sources.apply(descriptor.value1());
                if (!descriptorFile.isFile()) {
                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(descriptor.value1()).build());
                }
                return descriptorFile;
            }

            @Override
            protected boolean rollsBackOn(Exception e) {
                return !(e instanceof WebApplicationException);
            }
        });
    }

}
