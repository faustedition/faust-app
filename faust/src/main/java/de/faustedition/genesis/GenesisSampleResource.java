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

import de.faustedition.template.TemplateRepresentationFactory;

public class GenesisSampleResource extends ServerResource {

    private final TemplateRepresentationFactory viewFactory;

    private final GenesisSampleChart chart;

    @Inject
    public GenesisSampleResource(GenesisSampleChart chart, TemplateRepresentationFactory viewFactory) {
        super();
        this.chart = chart;
        this.viewFactory = viewFactory;
    }

    @Get("html")
    public TemplateRepresentation render() throws IOException {
        StringWriter imageMap = new StringWriter();
        chart.render(new ByteArrayOutputStream(), new PrintWriter(imageMap), "genesisChart");

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("imageMap", imageMap.toString());
        model.put("paralipomena", PARALIPOMENA_REFS);
        model.put("urfaust", URFAUST_REF);

        return viewFactory.create("genesis/index", getRequest().getClientInfo(), model);
    }

    static final List<String> WITNESSES = Lists.newArrayList("V.H15", "V.H13v", "V.H14", "V.H18", "V.H17r", "V.H2", "V.H16", "V.H");

    static final Map<String, LineInterval[]> GENESIS_DATASET = Maps.newLinkedHashMap();

    static {
        GENESIS_DATASET.put("V.H15", new LineInterval[] { new LineInterval("faust/2.5/gsa_391506.xml", "1", 11519, 11526) });
        GENESIS_DATASET.put("V.H13v", new LineInterval[] { new LineInterval("faust/2.5/gsa_391027.xml", "3", 11511, 11530) });
        GENESIS_DATASET.put("V.H14", new LineInterval[] { new LineInterval("faust/2.5/gsa_391505.xml", "1", 11511, 11530) });
        GENESIS_DATASET.put("V.H18", new LineInterval[] { new LineInterval("faust/2.5/gsa_390757.xml", "1", 11595, 11603) });
        GENESIS_DATASET.put("V.H17r", new LineInterval[] { new LineInterval("faust/2.5/gsa_391510.xml", "1", 11593, 11595) });
        GENESIS_DATASET.put("V.H2", new LineInterval[] {//
                new LineInterval("faust/2.5/gsa_390883.xml", "12", 11511, 11530),//
                        new LineInterval("faust/2.5/gsa_390883.xml", "13", 11539, 11590),//
                        new LineInterval("faust/2.5/gsa_390883.xml", "14", 11593, 11603) });
        GENESIS_DATASET.put("V.H16", new LineInterval[] { new LineInterval("faust/2.5/gsa_391507.xml", "1", 11573, 11576) });
        GENESIS_DATASET.put("V.H", new LineInterval[] {//
                new LineInterval("faust/2/gsa_391098.xml", "360", 11511, 11522),//
                        new LineInterval("faust/2/gsa_391098.xml", "415", 11523, 11543),//
                        new LineInterval("faust/2/gsa_391098.xml", "416", 11544, 11562),//
                        new LineInterval("faust/2/gsa_391098.xml", "417", 11563, 11586),//
                        new LineInterval("faust/2/gsa_391098.xml", "418", 11587, 11593),//
                        new LineInterval("faust/2/gsa_391098.xml", "419", 11594, 11619) });
    }

    static final List<ParalipomenonReference> PARALIPOMENA_REFS = Lists.newArrayList(//
            new ParalipomenonReference("P195", "faust/2.5/gsa_391082.xml", "1"),//
            new ParalipomenonReference("P21", "paralipomena/gsa_390782.xml", "1"),//
            new ParalipomenonReference("P1", "paralipomena/gsa_390720.xml", "1"),//
            new ParalipomenonReference("P93/P95", "paralipomena/gsa_390882.xml", "1"),//
            new ParalipomenonReference("P91", "paralipomena/gsa_391314.xml", "1"),//
            new ParalipomenonReference("P92a", "paralipomena/gsa_390781.xml", "1"),//
            new ParalipomenonReference("P92b", "paralipomena/gsa_390826.xml", "1"),//
            new ParalipomenonReference("P96", "paralipomena/gsa_390050.xml", "1"),//
            new ParalipomenonReference("P97", "faust/2/gsa_390777.xml", "1"),//
            new ParalipomenonReference("P98a", "faust/2/gsa_390705.xml", "1"),//
            new ParalipomenonReference("P98b", "faust/2/gsa_390705.xml", "2"));

    static final ParalipomenonReference URFAUST_REF = new ParalipomenonReference("Urfaust-Schluss", "gsa_390028.xml", "95");
}
