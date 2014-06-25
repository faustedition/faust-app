/*
 * Copyright (c) 2014 Faust Edition development team.
 *
 * This file is part of the Faust Edition.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.faustedition.document;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import de.faustedition.FaustURI;
import de.faustedition.graph.FaustGraph;
import de.faustedition.graph.FaustRelationshipType;
import de.faustedition.graph.NodeWrapperCollection;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.SortedSet;
import java.util.TreeSet;

import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

public class MaterialUnit extends NodeWrapperCollection<MaterialUnit> implements Comparable<MaterialUnit> {

	public enum Type {
		ARCHIVALDOCUMENT, DOCUMENT, SHEET, LEAF, DISJUNCTLEAF, PAGE, PATCH, PATCHSURFACE
	}

	protected static final String PREFIX = FaustGraph.PREFIX + ".material-unit";
	protected static final String METADATA_PREFIX = PREFIX + ".metadata.";

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

	public Archive getArchive() {
		for (MaterialUnit unit = this; unit != null; unit = unit.getParent()) {
			if (unit instanceof Document) {
				return Archive.getArchive((Document) unit);
			}
		}
		return null;
	}


	public MaterialUnit getParent() {
		// FIXME: we might want to have multiple parentship here too
		final Relationship r = node.getSingleRelationship(MATERIAL_PART_OF_RT, OUTGOING);
		return (r == null ? null : forNode(r.getStartNode()));
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
			case ARCHIVALDOCUMENT:
				return new Document(node);
			default:
				return new MaterialUnit(node);
		}
	}

	public FaustURI getFacsimile() {
		final String uri = (String) node.getProperty(PREFIX + ".facsimile", null);
		return (uri == null ? null :FaustURI.parse(uri));
	}

	public Type getType() {
		return getType(node);
	}

	public void setFacsimile(FaustURI uri) {
		node.setProperty(PREFIX + ".facsimile", uri.toString());
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
	
	public Iterable<String> getMetadataKeys() {
		
		Iterable<String> prefixFiltered = Iterables.filter(node.getPropertyKeys(), new Predicate<String>() {
			@Override
			public boolean apply(String input) {
				return input.startsWith(METADATA_PREFIX);
			}
		});
		
		Iterable<String> prefixPruned =  Iterables.transform(prefixFiltered, new Function<String, String>() {
		@Override
		public String apply(String input) {
			return input.substring(METADATA_PREFIX.length());
		}});
		
		return prefixPruned;
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

	@Override
	public String toString() {

        final String waFaust = getMetadataValue("callnumber.wa-faust");
		if (!Strings.isNullOrEmpty(waFaust) && !"none".equals(waFaust)) {
			return waFaust;
		}

		final String gsaNew = getMetadataValue("callnumber.gsa_2");
		if (!Strings.isNullOrEmpty(gsaNew) && !"none".equals(gsaNew)) {
			return gsaNew;
		}
		
		final String gsaOld = getMetadataValue("callnumber.gsa_1");
		if (!Strings.isNullOrEmpty(gsaOld) && !"none".equals(gsaOld)) {
			return gsaOld;
		}
		
		final String waId = getMetadataValue("wa-id");
		if (!Strings.isNullOrEmpty(waId) && !"none".equals(waId)) {
			return waId;
		}
		
		final String callnumber = getMetadataValue("callnumber");
		if (!Strings.isNullOrEmpty(callnumber) && !"none".equals(callnumber)) {
			return new StringBuilder(getArchive().getId()).append("/").append(callnumber).toString();
		}
		
		final FaustURI transcriptSource = getTranscriptSource();
		if (transcriptSource != null) {
			return transcriptSource.getFilename();
		}

		return super.toString();
	}
}
