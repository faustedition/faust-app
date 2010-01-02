package de.faustedition.model.hierarchy;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.support.DataAccessUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.faustedition.model.document.ArchiveReference;
import de.faustedition.model.document.DocumentDating;
import de.faustedition.model.document.LegacyMetadata;
import de.faustedition.model.document.PrintReference;
import de.faustedition.model.document.TranscriptionDocument;
import de.faustedition.model.facsimile.FacsimileAssociation;
import de.faustedition.util.HibernateUtil;

public abstract class HierarchyNodeFacet {
	protected long id;
	protected HierarchyNode facettedNode;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public HierarchyNode getFacettedNode() {
		return facettedNode;
	}

	public void setFacettedNode(HierarchyNode facettedNode) {
		this.facettedNode = facettedNode;
	}

	@Override
	public boolean equals(Object obj) {
		if ((obj != null) && (getClass().equals(obj.getClass()))) {
			HierarchyNodeFacet other = (HierarchyNodeFacet) obj;
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

	public static Map<Class<? extends HierarchyNodeFacet>, HierarchyNodeFacet> findByNode(Session session, HierarchyNode node) {
		Map<Class<? extends HierarchyNodeFacet>, HierarchyNodeFacet> facets = Maps.newHashMap();
		Criteria facetCriteria = session.createCriteria(HierarchyNodeFacet.class);
		facetCriteria = facetCriteria.createCriteria("facettedNode").add(Restrictions.idEq(node.getId()));
		for (HierarchyNodeFacet facet : HibernateUtil.scroll(facetCriteria, HierarchyNodeFacet.class)) {
			facets.put(facet.getClass(), facet);
		}
		return facets;
	}

	public static <T> T findByNode(Session session, HierarchyNode node, Class<T> facetType) {
		Criteria facetCriteria = session.createCriteria(facetType);
		facetCriteria = facetCriteria.createCriteria("facettedNode").add(Restrictions.idEq(node.getId()));
		return DataAccessUtils.uniqueResult(HibernateUtil.list(facetCriteria, facetType));
	}

	public static List<HierarchyNodeFacet> sortedList(Collection<HierarchyNodeFacet> facets) {
		List<HierarchyNodeFacet> sortedList = Lists.newArrayList(facets);
		Collections.sort(sortedList, NODE_FACET_COMPARATOR);
		return sortedList;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", id).append("node", facettedNode).toString();
	}

	@SuppressWarnings("unchecked")
	private static final Comparator<HierarchyNodeFacet> NODE_FACET_COMPARATOR = new Comparator<HierarchyNodeFacet>() {

		private final List<Class<? extends HierarchyNodeFacet>> ORDER_LIST = Lists.newArrayList(FacsimileAssociation.class,
				TranscriptionDocument.class, ArchiveReference.class, DocumentDating.class, PrintReference.class,
				LegacyMetadata.class);

		@Override
		public int compare(HierarchyNodeFacet o1, HierarchyNodeFacet o2) {
			int o1Index = ORDER_LIST.indexOf(o1.getClass());
			int o2Index = ORDER_LIST.indexOf(o2.getClass());
			if (o1Index < 0 || o2Index < 0) {
				throw new IllegalArgumentException();
			}
			return (o1Index - o2Index);
		}

	};
}
