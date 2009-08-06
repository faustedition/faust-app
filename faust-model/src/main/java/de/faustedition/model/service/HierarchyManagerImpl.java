package de.faustedition.model.service;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.faustedition.model.HierarchyNode;
import de.faustedition.model.HierarchyNodeType;
import de.faustedition.model.Model;

@Service
@Transactional(readOnly = true)
public class HierarchyManagerImpl implements HierarchyManager {

	@Autowired
	private SessionFactory sessionFactory;

	public HierarchyNode findRoot() {
		return HierarchyNode.findRoot(sessionFactory.getCurrentSession());
	}

	@Transactional(readOnly = false)
	public void initHierarchy() {
		Session session = sessionFactory.getCurrentSession();
		try {
			HierarchyNode.findRoot(session);
		} catch (IncorrectResultSizeDataAccessException e) {
			HierarchyNode.createRoot().save(session);
		}
	}

	@Transactional(readOnly = false)
	public void clear() {
		HierarchyNode.clear(sessionFactory.getCurrentSession());
	}

	@Transactional(readOnly = false)
	public HierarchyNode createNode(HierarchyNode parent, String name, HierarchyNodeType type) {
		if (nodeExists(parent, name)) {
			throw new DataIntegrityViolationException(String.format("'%s' already exists in '%s'", name, parent.getFullPath()));
		}
		return parent.add(sessionFactory.getCurrentSession(), new HierarchyNode(name, type));
	}

	public boolean nodeExists(HierarchyNode parent, String name) {
		return parent.hasChild(sessionFactory.getCurrentSession(), name);
	}

	public HierarchyNode findNode(HierarchyNode parent, String name) {
		return parent.findChild(sessionFactory.getCurrentSession(), name);
	}

	public HierarchyNode loadNode(int id) {
		return Model.load(sessionFactory.getCurrentSession(), HierarchyNode.class, id);
	}

	public HierarchyNode loadByPath(String nodePath) {
		return HierarchyNode.loadByPath(sessionFactory.getCurrentSession(), nodePath);
	}

	public List<HierarchyNode> findChildren(HierarchyNode node) {
		return node.findChildren(sessionFactory.getCurrentSession());
	}
}
