package de.faustedition.web.document;

import java.util.Deque;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import de.faustedition.model.ObjectNotFoundException;
import de.faustedition.model.document.DocumentStructureNode;
import de.faustedition.web.ControllerUtil;

@Controller
public class DocumentStructureNodeController
{

	private static final String URL_PREFIX = "document";

	@Autowired
	private SessionFactory sessionFactory;

	@RequestMapping("/" + URL_PREFIX + "/**")
	public String browse(HttpServletRequest request, ModelMap model) throws ObjectNotFoundException
	{
		Deque<String> pathComponents = ControllerUtil.getPathComponents(request);
		if (!pathComponents.isEmpty() && URL_PREFIX.equals(pathComponents.getFirst()))
		{
			pathComponents.removeFirst();
		}

		Session session = sessionFactory.getCurrentSession();
		DocumentStructureNode node = null;
		if (!pathComponents.isEmpty())
		{
			node = ControllerUtil.foundObject(DocumentStructureNode.findByPath(session, pathComponents));
		}
		if (node != null)
		{
			model.addAttribute("node", node);
			model.addAttribute("parentPath", DocumentStructureNode.findParents(session, pathComponents));
		}

		model.addAttribute("children", node == null ? DocumentStructureNode.findRootChildren(session) : node.findChildren(session));

		return "document/node";
	}
}
