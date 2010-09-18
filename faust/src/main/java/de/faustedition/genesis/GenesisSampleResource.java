package de.faustedition.genesis;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.faustedition.template.TemplateRepresentationFactory;

public class GenesisSampleResource extends ServerResource {

    private final String contextPath;

    private final TemplateRepresentationFactory viewFactory;

    @Inject
    public GenesisSampleResource(TemplateRepresentationFactory viewFactory, @Named("ctx.path") String contextPath) {
        super();
        this.viewFactory = viewFactory;
        this.contextPath = contextPath;
    }

    @Get("html")
    public TemplateRepresentation render() throws IOException {
        StringWriter imageMap = new StringWriter();
        new GenesisSampleChartResource.GenesisExampleChart().render(new ByteArrayOutputStream(), new PrintWriter(imageMap),
                contextPath + "/document/", "genesisChart");

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("imageMap", imageMap.toString());
        model.put("paralipomena", PARALIPOMENA_REFS);
        model.put("urfaust", URFAUST_REF);

        return viewFactory.create("genesis/index", getRequest().getClientInfo(), model);
    }

    static final List<String> WITNESSES = Lists.newArrayList("V.H15", "V.H13v", "V.H14", "V.H18", "V.H17r", "V.H2", "V.H16", "V.H");

    static final Map<String, LineInterval[]> GENESIS_DATASET = Maps.newLinkedHashMap();

    static {
        GENESIS_DATASET.put("V.H15", new LineInterval[] { new LineInterval("391506", "0002", 11519, 11526) });
        GENESIS_DATASET.put("V.H13v", new LineInterval[] { new LineInterval("391027", "0004", 11511, 11530) });
        GENESIS_DATASET.put("V.H14", new LineInterval[] { new LineInterval("391505", "0002", 11511, 11530) });
        GENESIS_DATASET.put("V.H18", new LineInterval[] { new LineInterval("390757", "0002", 11595, 11603) });
        GENESIS_DATASET.put("V.H17r", new LineInterval[] { new LineInterval("391510", "0002", 11593, 11595) });
        GENESIS_DATASET.put("V.H2", new LineInterval[] { new LineInterval("390883", "0013", 11511, 11530),//
                new LineInterval("390883", "0014", 11539, 11590),//
                new LineInterval("390883", "0015", 11593, 11603) });
        GENESIS_DATASET.put("V.H16", new LineInterval[] { new LineInterval("391507", "0002", 11573, 11576) });
        GENESIS_DATASET.put("V.H", new LineInterval[] { new LineInterval("391098", "0360", 11511, 11522),//
                new LineInterval("391098", "0361", 11523, 11543),//
                new LineInterval("391098", "0362", 11544, 11562),//
                new LineInterval("391098", "0363", 11563, 11586),//
                new LineInterval("391098", "0364", 11587, 11593),//
                new LineInterval("391098", "0365", 11594, 11619) });
    }

    static final List<ParalipomenonReference> PARALIPOMENA_REFS = Lists.newArrayList(new ParalipomenonReference("P195", "391082",
            "0002"),//
            new ParalipomenonReference("P21", "390782", "0002"),//
            new ParalipomenonReference("P1", "390720", "0002"),//
            new ParalipomenonReference("P93/P95", "390882", "0002"),//
            new ParalipomenonReference("P91", "391314", "0002"),//
            new ParalipomenonReference("P92a", "390781", "0002"),//
            new ParalipomenonReference("P92b", "390826", "0002"),//
            new ParalipomenonReference("P96", "390050", "0002"),//
            new ParalipomenonReference("P97", "390777", "0002"),//
            new ParalipomenonReference("P98a", "390705", "0002"),//
            new ParalipomenonReference("P98b", "390705", "0003"));

    static final ParalipomenonReference URFAUST_REF = new ParalipomenonReference("Urfaust-Schluss", "390028", "0095");
}
