package de.faustedition.reasoning;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.FileBackedOutputStream;
import de.faustedition.Database;
import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.document.MaterialUnit;
import de.faustedition.genesis.GeneticSource;
import de.faustedition.graph.Graph;
import de.faustedition.reasoning.PremiseBasedRelation.Premise;
import de.faustedition.text.VerseInterval;
import de.faustedition.transcript.TranscribedVerseIntervalCollector;
import edu.bath.transitivityutils.ImmutableRelation;
import edu.bath.transitivityutils.Relation;
import edu.bath.transitivityutils.Relations;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Path("/genesis/inscriptions")
@Singleton
public class InscriptionPrecedenceResource {

    private final GraphDatabaseService graphDb;
    private final Database database;
    private final Logger logger;
    private final String tredPath;
    private final String dotPath;

    private final Set<Inscription> inscriptions = Sets.newHashSet();
    private Relation<Inscription> syntagmaticPrecedence = Relations.newTransitiveRelation();
    private Relation<Inscription> exclusiveContainment = Relations.newTransitiveRelation();//MultimapBasedRelation.create();
    private Relation<Inscription> paradigmaticContainment = Relations.newTransitiveRelation(); //MultimapBasedRelation.create();
    private ImmutableRelation<Inscription> explicitPrecedence;


    @Inject
    public InscriptionPrecedenceResource(GraphDatabaseService graphDb, Database database, Logger logger) {
        this.graphDb = graphDb;
        this.database = database;
        this.logger = logger;
        this.tredPath = System.getProperty("faust.tred", "/usr/bin/tred");
        this.dotPath = System.getProperty("faust.dot", "/usr/bin/dot");
    }

    @Path("/{part}/{act_scene}")
    public String act(@PathParam("part") int part, @PathParam("act_scene") int act) {
        final VerseInterval verseInterval = VerseInterval.fromRequestAttibutes(part, act, 0);
        return null;
    }

    @Path("/{part}/{act_scene}/{scene}")
    public String scene(@PathParam("part") int part, @PathParam("act_scene") int act, @PathParam("scene") int scene) {
        final VerseInterval verseInterval = VerseInterval.fromRequestAttibutes(part, act, scene);
        return null;
    }

    Map<Inscription, Node> nodeMap(VerseInterval verseInterval) {
        final Map<Inscription, Node> nodeMap = Maps.newHashMap();

        // FIXME: query
        //final Map<MaterialUnit, Collection<VerseInterval>> intervalIndex = TranscribedVerseIntervalCollector.byMaterialUnit(dataSource, graphDb, verseInterval.getStart(), verseInterval.getEnd());
        for (Map.Entry<MaterialUnit, Collection<VerseInterval>> intervals : Collections.<MaterialUnit, Collection<VerseInterval>>emptyMap().entrySet()) {
            final MaterialUnit materialUnit = intervals.getKey();
            final String sigil = materialUnit.toString();
            final Inscription inscription = new Inscription(sigil);
            for (VerseInterval interval : intervals.getValue()) {
                inscription.addInterval(interval.getStart(), interval.getEnd());
            }
            Preconditions.checkState(!inscription.isEmpty());
            inscriptions.add(inscription);
            nodeMap.put(inscription, materialUnit.node);
        }

        for (Inscription subject : inscriptions) {
            for (Inscription object : inscriptions) {
                if (InscriptionRelations.syntagmaticallyPrecedesByFirstLine(subject, object)) {
//				if (InscriptionRelations.syntagmaticallyPrecedesByAverage(subject, object)) {

                    syntagmaticPrecedence.relate(subject, object);
                }
                if (InscriptionRelations.exclusivelyContains(subject, object)) {
                    exclusiveContainment.relate(subject, object);
                }
                if (InscriptionRelations.paradigmaticallyContains(subject, object)) {
                    paradigmaticContainment.relate(subject, object);
                }

            }
        }

        explicitPrecedence = new GraphBasedRelation<Inscription>(nodeMap, new FaustURI(FaustAuthority.SECONDARY, "/gruss2011"));


        List<Premise<Inscription>> premises = new ArrayList<Premise<Inscription>>();
//		premises.addAll(premisesFromGeneticSources());
        premises.addAll(premisesFromInference());

        precedence = new PremiseBasedRelation<Inscription>(premises);
//		precedence = new LastPremiseRelation<Inscription> (premises);

        Relation<Inscription> test = Util.wrapTransitive(
                new PremiseBasedRelation<Inscription>(premisesFromInference()
        ), inscriptions);


        Relation<Inscription> check = Util.wrapTransitive(
                new PremiseBasedRelation<Inscription>(premisesFromGeneticSources()
        ), inscriptions);

        logger.info("Genetic graph statistics: ");
        logger.info(
                "  Coverage: " + Statistics.completeness(precedence, check, inscriptions) * 100 +
                        ", Recall: " + Statistics.recall(precedence, check, inscriptions) * 100 +
                        ", Accuracy : " + Statistics.correctness(precedence, check, inscriptions) * 100

        );

        return nodeMap;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String dot() {
        return asDot();
    }

    @GET
    @Produces({"image/svg+xml", MediaType.TEXT_HTML})
    public StreamingOutput svg() throws IOException, ExecutionException, InterruptedException {
        final ExecutorService executorService = Executors.newCachedThreadPool();
        final Process tred = new ProcessBuilder(tredPath).start();
        final Process dot = new ProcessBuilder(dotPath, "-Tsvg").start();

        executorService.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                InputStream dataStream = null;
                OutputStream tredStream = null;
                try {
                    ByteStreams.copy(dataStream = new ByteArrayInputStream(asDot().getBytes(Charset.forName("UTF-8"))), tredStream = tred.getOutputStream());
                } finally {
                    Closeables.close(dataStream, false);
                    Closeables.close(tredStream, false);
                }
                return null;
            }
        });

        executorService.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                InputStream tredStream = null;
                OutputStream dotStream = null;
                try {
                    ByteStreams.copy(tredStream = tred.getInputStream(), dotStream = dot.getOutputStream());
                } finally {
                    Closeables.close(tredStream, false);
                    Closeables.close(dotStream, false);
                }
                return null;
            }
        });

        final Future<FileBackedOutputStream> dotFuture = executorService.submit(new Callable<FileBackedOutputStream>() {
            @Override
            public FileBackedOutputStream call() throws Exception {
                final FileBackedOutputStream buf = new FileBackedOutputStream(102400);
                InputStream dotStream = null;
                try {
                    ByteStreams.copy(dotStream = dot.getInputStream(), buf);
                } finally {
                    Closeables.close(dotStream, false);
                    Closeables.close(buf, false);
                }
                return buf;
            }
        });

        Preconditions.checkState(tred.waitFor() == 0);
        Preconditions.checkState(dot.waitFor() == 0);

        final FileBackedOutputStream resultBuf = dotFuture.get();

        return new StreamingOutput() {

            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                resultBuf.asByteSource().copyTo(output);
                resultBuf.reset();
            }
        };
    }

    private String asDot() {
        final StringBuilder dot = new StringBuilder("digraph genetic_graph {\n");

        for (Inscription inscription : inscriptions) {
            dot.append(toLabel(inscription));

            dot.append(" [label=\"");
            dot.append(toLabel(inscription));
            dot.append("\\n");
            dot.append(inscription.first());
            dot.append("...");
            dot.append(inscription.last());
            dot.append("\"]");

            dot.append(";\n");

        }

        dot.append(" edge [");
        dot.append(" color=").append("black");
        dot.append(" fontcolor=").append("black");
        dot.append(" weight=").append("1");
        dot.append(" ];\n");

        for (Inscription i : inscriptions) {
            for (Inscription j : inscriptions) {
                if (precedence.areRelated(i, j)) {
                    final String premise = precedence.findRelevantPremise(i, j).getName();

                    dot.append(toLabel(i));
                    dot.append(" -> ");
                    dot.append(toLabel(j));
                    dot.append(" [ ");
                    dot.append(" label=").append(premise);
                    dot.append(" color=").append("r_syn".equals(premise) ? "grey" : "black");
                    dot.append(" weight=").append("r_syn".equals(premise) ? "1" : "1");
                    dot.append(" ];\n");
                }
            }
        }

        dot.append("}\n");
        return dot.toString();
    }

    private String toLabel(Inscription inscription) {
        return inscription.getName().replaceAll("[ ,:\\(\\)\\-\\.\\{\\}/]", "_");
    }

    private List<Premise<Inscription>> premisesFromGeneticSources() {
        // FIXME!!!
        Graph graph = null;
        Map<Inscription, Node> nodeMap = null;

        List<Premise<Inscription>> result = new ArrayList<Premise<Inscription>>();
        for (final GeneticSource geneticSource : graph.getGeneticSources()) {
            final GraphBasedRelation<Inscription> gbr = new GraphBasedRelation<Inscription>(nodeMap, geneticSource.getUri());
            Premise<Inscription> premise = new Premise<Inscription>() {
                @Override
                public String getName() {
                    String name;
                    try {
                        name = geneticSource.getUri().toString().substring("faust://secondary/".length());
                    } catch (Exception e) {
                        name = geneticSource.getUri().toString();
                    }
                    return name.replaceAll("[:/]", "_");
                }

                public boolean applies(Inscription i, Inscription j) {

                    return gbr.areRelated(i, j);
                }
            };
            result.add(premise);
        }
        return result;
    }

    private List<Premise<Inscription>> premisesFromInference() {
        ArrayList<Premise<Inscription>> result = new ArrayList<Premise<Inscription>>();


        Premise<Inscription> rEcon = new Premise<Inscription>() {
            @Override
            public String getName() {
                return "r_econ";
            }

            @Override
            public boolean applies(Inscription i, Inscription j) {
                return exclusiveContainment.areRelated(i, j);
            }
        };

        Premise<Inscription> rPcon = new PremiseBasedRelation.Premise<Inscription>() {
            @Override
            public String getName() {
                return "r_pcon";
            }

            @Override
            public boolean applies(Inscription i, Inscription j) {
                return paradigmaticContainment.areRelated(j, i);
            }
        };

        Premise<Inscription> rSyn = new PremiseBasedRelation.Premise<Inscription>() {
            @Override
            public String getName() {
                return "r_syn";
            }

            @Override
            public boolean applies(Inscription i, Inscription j) {
                return syntagmaticPrecedence.areRelated(i, j);
            }
        };

        result.add(rPcon);
        result.add(rEcon);
        result.add(rSyn);

        return result;
    }

    public PremiseBasedRelation<Inscription> precedence;


    /**
     * @param relA
     * @param relB is supposed to be the overriding or "stronger" relation
     * @return A relation result where result(i, j) if relA(i,j) && relB(j,i)
     */
    public Relation<Inscription> contradictions(ImmutableRelation<Inscription> relA, ImmutableRelation<Inscription> relB) {
        Relation<Inscription> result = MultimapBasedRelation.create();
        for (Inscription i : inscriptions)
            for (Inscription j : inscriptions) {
                if (relA.areRelated(i, j) && relB.areRelated(j, i)) {
                    result.relate(i, j);
                }
            }
        return result;
    }

}
