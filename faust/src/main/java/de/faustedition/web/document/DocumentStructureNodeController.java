package de.faustedition.web.document;

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
import de.faustedition.model.document.DocumentStructureNode;
import de.faustedition.model.document.DocumentStructureNodeFacet;
import de.faustedition.model.document.TranscriptionFacet;
import de.faustedition.model.document.TranscriptionStatus;
import de.faustedition.web.ControllerUtil;

@Controller
public class DocumentStructureNodeController {

	private static final String URL_PREFIX = "document";

	@Autowired
	private SessionFactory sessionFactory;

	@RequestMapping("/" + URL_PREFIX + "/**")
	public String browse(HttpServletRequest request, ModelMap model) throws ObjectNotFoundException {
		Deque<String> pathComponents = ControllerUtil.getPathComponents(request);
		if (!pathComponents.isEmpty() && URL_PREFIX.equals(pathComponents.getFirst())) {
			pathComponents.removeFirst();
		}

		Session session = sessionFactory.getCurrentSession();
		DocumentStructureNode node = null;
		if (!pathComponents.isEmpty()) {
			node = ControllerUtil.foundObject(DocumentStructureNode.findByPath(session, pathComponents));
		}
		if (node != null) {
			model.addAttribute("node", node);
			model.addAttribute("parentPath", DocumentStructureNode.findParents(session, pathComponents));
			model.addAttribute("facets", DocumentStructureNodeFacet.sortedList(DocumentStructureNodeFacet.findByNode(
					session, node).values()));
			model.addAttribute("children", node.findChildren(session));
			model.addAttribute("transcriptionStatus", buildTranscriptionStatusModel(session, node));
		} else {
			model.addAttribute("children", DocumentStructureNode.findRootChildren(session));
			model.addAttribute("transcriptionStatus", buildTranscriptionStatusModel(session, null));
		}

		return "document/node";
	}

	private SortedMap<String, Integer> buildTranscriptionStatusModel(Session session, DocumentStructureNode node) {
		SortedMap<TranscriptionStatus, Integer> stati = TranscriptionFacet.summarizeTranscriptionStatus(session, node);
		SortedMap<String, Integer> model = new TreeMap<String, Integer>();
		for (TranscriptionStatus status : stati.keySet()) {
			model.put(Integer.toString(status.ordinal()) + "_" + status.toString(), stati.get(status));
		}
		return model;
	}
}
