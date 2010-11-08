package de.faustedition.graph;

import org.neo4j.graphdb.RelationshipType;

public class FaustRelationshipType implements RelationshipType {

    private final String name;

    public FaustRelationshipType(String name) {
        this.name = FaustGraph.PREFIX + "." + name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && (obj instanceof RelationshipType)) {
            return name.equals(((RelationshipType) obj).name());
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
