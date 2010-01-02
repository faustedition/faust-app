package de.faustedition.web.hierarchy;

import java.util.Deque;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import de.faustedition.model.ObjectNotFoundException;
import de.faustedition.model.document.TranscriptionDocument;
import de.faustedition.model.document.TranscriptionStatus;
import de.faustedition.model.hierarchy.HierarchyNode;
import de.faustedition.model.hierarchy.HierarchyNodeFacet;
import de.faustedition.web.ControllerUtil;

@Controller
public class BrowseHierarchyController {

	private static final String URL_PREFIX = "browse";

	@Autowired
	private SessionFactory sessionFactory;

	@RequestMapping("/" + URL_PREFIX + "/**")
	public String browse(HttpServletRequest request, ModelMap model) throws ObjectNotFoundException {
		Deque<String> pathComponents = ControllerUtil.getPathComponents(request);
		if (!pathComponents.isEmpty() && URL_PREFIX.equals(pathComponents.getFirst())) {
			pathComponents.removeFirst();
		}

		Session session = sessionFactory.getCurrentSession();
		HierarchyNode node = ControllerUtil.foundObject(HierarchyNode.findByPath(session, pathComponents));
		model.addAttribute("node", node);
		model.addAttribute("parentPath", node.findParents(session));
		model.addAttribute("facets", HierarchyNodeFacet.sortedList(HierarchyNodeFacet.findByNode(session, node).values()));
		model.addAttribute("children", node.findChildren(session));
		model.addAttribute("transcriptionStatus", buildTranscriptionStatusModel(session, node));
		return "browse/node";
	}

	private SortedMap<String, Integer> buildTranscriptionStatusModel(Session session, HierarchyNode node) {
		SortedMap<TranscriptionStatus, Integer> stati = TranscriptionDocument.summarizeTranscriptionStatus(session, node);
		SortedMap<String, Integer> model = new TreeMap<String, Integer>();
		for (TranscriptionStatus status : stati.keySet()) {
			model.put(Integer.toString(status.ordinal()) + "_" + status.toString(), stati.get(status));
		}
		return model;
	}
}
