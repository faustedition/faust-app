package de.faustedition.model.dav;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.classic.Session;

import com.bradmcevoy.http.Resource;

import de.faustedition.model.hierarchy.HierarchyNode;
import de.faustedition.model.hierarchy.HierarchyNodeFacet;

public class HierarchyNodeDavResource extends CollectionDavResourceBase {
	private final HierarchyNode node;

	protected HierarchyNodeDavResource(DavResourceFactory factory, HierarchyNode node) {
		super(factory);
		this.node = node;
	}

	@Override
	public String getName() {
		return node.getName();
	}

	@Override
	public Resource child(String childName) {
		HierarchyNode child = node.findChild(factory.getDbSessionFactory().getCurrentSession(), childName);
		return (child == null ? factory.createResource(node, childName) : factory.createResource(child, null));
	}

	@Override
	public List<? extends Resource> getChildren() {
		Session session = factory.getDbSessionFactory().getCurrentSession();
		
		List<HierarchyNode> childNodes = node.findChildren(session);
		List<Resource> children = new ArrayList<Resource>(childNodes.size());
		for (HierarchyNode childNode : childNodes) {
			children.add(factory.createResource(childNode));
		}

		for (HierarchyNodeFacet facet : HierarchyNodeFacet.findByNode(session, node).values()) {
			Resource resource = factory.createResource(facet);
			if (resource != null) {
				children.add(resource);
			}
		}
		
		return children;
	}

	@Override
	public Object getLockResource() {
		return node;
	}
}
