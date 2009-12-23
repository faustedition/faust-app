package de.faustedition.model.document;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.faustedition.util.HibernateUtil;

public abstract class DocumentStructureNodeFacet {
	protected long id;
	protected DocumentStructureNode facettedNode;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public DocumentStructureNode getFacettedNode() {
		return facettedNode;
	}

	public void setFacettedNode(DocumentStructureNode facettedNode) {
		this.facettedNode = facettedNode;
	}

	@Override
	public boolean equals(Object obj) {
		if ((obj != null) && (getClass().equals(obj.getClass()))) {
			DocumentStructureNodeFacet other = (DocumentStructureNodeFacet) obj;
			return facettedNode.equals(other.facettedNode);
		}

		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return facettedNode.hashCode();
	}

	public void save(Session session) {
		session.saveOrUpdate(this);
	}

	public static Map<Class<? extends DocumentStructureNodeFacet>, DocumentStructureNodeFacet> findByNode(Session session,
			DocumentStructureNode node) {
		Map<Class<? extends DocumentStructureNodeFacet>, DocumentStructureNodeFacet> facets = Maps.newHashMap();
		Criteria facetCriteria = session.createCriteria(DocumentStructureNodeFacet.class);
		facetCriteria = facetCriteria.createCriteria("facettedNode").add(Restrictions.idEq(node.getId()));
		for (DocumentStructureNodeFacet facet : HibernateUtil.scroll(facetCriteria, DocumentStructureNodeFacet.class)) {
			facets.put(facet.getClass(), facet);
		}
		return facets;
	}

	public static List<DocumentStructureNodeFacet> sortedList(Collection<DocumentStructureNodeFacet> facets) {
		List<DocumentStructureNodeFacet> sortedList = Lists.newArrayList(facets);
		Collections.sort(sortedList, NODE_FACET_COMPARATOR);
		return sortedList;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", id).append("node", facettedNode).toString();
	}

	@SuppressWarnings("unchecked")
	private static final Comparator<DocumentStructureNodeFacet> NODE_FACET_COMPARATOR = new Comparator<DocumentStructureNodeFacet>() {

		private final List<Class<? extends DocumentStructureNodeFacet>> ORDER_LIST = Lists.newArrayList(
				FacsimileFacet.class, TranscriptionFacet.class, ArchiveFacet.class, DatingFacet.class,
				PrintReferenceFacet.class, LegacyMetadataFacet.class);

		@Override
		public int compare(DocumentStructureNodeFacet o1, DocumentStructureNodeFacet o2) {
			int o1Index = ORDER_LIST.indexOf(o1.getClass());
			int o2Index = ORDER_LIST.indexOf(o2.getClass());
			if (o1Index < 0 || o2Index < 0) {
				throw new IllegalArgumentException();
			}
			return (o1Index - o2Index);
		}

	};
}
