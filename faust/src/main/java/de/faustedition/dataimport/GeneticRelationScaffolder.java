package de.faustedition.dataimport;

import com.google.inject.Inject;

import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.Runtime;
import de.faustedition.xml.XMLStorage;

public class GeneticRelationScaffolder extends Runtime {

    private final XMLStorage xml;

    @Inject
    public GeneticRelationScaffolder(XMLStorage xml) {
        this.xml = xml;
    }

    public static void main(String[] args) throws Exception {
        Runtime.main(GeneticRelationScaffolder.class, args);
    }

    @Override
    public void run() {
        for (FaustURI source : xml.iterate(new FaustURI(FaustAuthority.XML, "document"))) {
            System.out.println(source);
        }
    }
}
