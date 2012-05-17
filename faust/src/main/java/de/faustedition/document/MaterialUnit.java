package de.faustedition.document;

import de.faustedition.FaustURI;
import de.faustedition.graph.FaustGraph;
import de.faustedition.graph.FaustRelationshipType;
import de.faustedition.graph.NodeWrapper;
import de.faustedition.graph.NodeWrapperCollection;
import de.faustedition.transcript.GoddagTranscript;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.SortedSet;
import java.util.TreeSet;

import static de.faustedition.transcript.GoddagTranscript.TRANSCRIPT_RT;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

public class MaterialUnit extends NodeWrapperCollection<MaterialUnit> implements Comparable<MaterialUnit> {
	public enum Type {
		ARCHIVAL_UNIT, DOCUMENT, QUIRE, SHEET, FOLIO, PAGE, SURFACE
	}

	private static final String PREFIX = FaustGraph.PREFIX + ".material-unit";
	private static final String METADATA_PREFIX = PREFIX + ".metadata.";

	private static final FaustRelationshipType MATERIAL_PART_OF_RT = new FaustRelationshipType("is-material-part-of");

	public MaterialUnit(Node node) {
		super(node, MaterialUnit.class, MATERIAL_PART_OF_RT);
	}

	public MaterialUnit(Node node, Type type) {
		this(node);
		setType(type);
		node.setProperty(PREFIX + ".last-modified", System.currentTimeMillis());
	}

	public long created() {
		return (Long) node.getProperty(PREFIX + ".last-modified");
	}

	public MaterialUnit getParent() {
		// FIXME: we might want to have multiple parentship here too
		final Relationship r = node.getSingleRelationship(MATERIAL_PART_OF_RT, OUTGOING);
		if (r == null) {
			return null;
		}
		final MaterialUnit mu = NodeWrapper.newInstance(MaterialUnit.class, r.getStartNode());
		switch (mu.getType()) {
			case ARCHIVAL_UNIT:
			case DOCUMENT:
				return new Document(mu.node);
			default:
				return mu;
		}
	}

	public SortedSet<MaterialUnit> getSortedContents() {
		final TreeSet<MaterialUnit> set = new TreeSet<MaterialUnit>();
		addRecursively(set, this);
		return set;
	}

	protected void addRecursively(TreeSet<MaterialUnit> set, MaterialUnit materialUnit) {
		for (MaterialUnit child : materialUnit) {
			set.add(child);
			addRecursively(set, child);
		}
	}

	public static Type getType(Node node) {
		return Type.valueOf(((String) node.getProperty(PREFIX + ".type")).replaceAll("\\-", "_").toUpperCase());
	}

	public static MaterialUnit forNode(Node node) {
		switch (getType(node)) {
			case DOCUMENT:
			case ARCHIVAL_UNIT:
				return new Document(node);
			default:
				return new MaterialUnit(node);
		}
	}

	public Type getType() {
		return getType(node);
	}

	public void setType(Type type) {
		node.setProperty(PREFIX + ".type", type.name().toLowerCase().replaceAll("_", "-"));
	}

	public void setOrder(int order) {
		node.setProperty(PREFIX + ".order", order);
	}

	public int getOrder() {
		return (Integer) node.getProperty(PREFIX + ".order", -1);
	}

	public static MaterialUnit find(GoddagTranscript t) {
		for (Relationship r : t.node.getRelationships(TRANSCRIPT_RT, OUTGOING)) {
			return forNode(r.getEndNode());
		}
		return null;
	}

	public GoddagTranscript getTranscript() {
		final Relationship r = node.getSingleRelationship(TRANSCRIPT_RT, INCOMING);
		return (r == null ? null : GoddagTranscript.forNode(r.getStartNode()));
	}

	public void setTranscript(GoddagTranscript transcript) {
		final Relationship r = node.getSingleRelationship(TRANSCRIPT_RT, INCOMING);
		if (r != null) {
			r.delete();
		}
		if (transcript != null) {
			transcript.node.createRelationshipTo(node, TRANSCRIPT_RT);
		}
	}

	public FaustURI getTranscriptSource() {
		final String uri = (String) node.getProperty(PREFIX + ".transcript", null);
		return (uri == null ? null :FaustURI.parse(uri));
	}

	public void setTranscriptSource(FaustURI source) {
		node.setProperty(PREFIX + ".transcript", source.toString());
	}

	public String getMetadataValue(String key) {
		final String[] metadata = getMetadata(key);
		return (metadata == null ? null : metadata[0]);
	}

	public String[] getMetadata(String key) {
		final String metadataKey = METADATA_PREFIX + key;
		return node.hasProperty(metadataKey) ? (String[]) node.getProperty(metadataKey) : null;
	}

	public void setMetadata(String key, String[] values) {
		final String metadataKey = METADATA_PREFIX + key;

		if (values == null || values.length == 0) {
			node.removeProperty(metadataKey);
		} else {
			node.setProperty(metadataKey, values);
		}
	}

	@Override
	public int compareTo(MaterialUnit o) {
		final int o1 = getOrder();
		final int o2 = o.getOrder();
		return (o1 >= 0 && o2 >= 0) ? (o1 - o2) : 0;
	}

}
