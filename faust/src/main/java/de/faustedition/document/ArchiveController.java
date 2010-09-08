package de.faustedition.document;

import static de.faustedition.xml.NodeListIterable.singleResult;
import static de.faustedition.xml.XPathUtil.xpath;

import java.io.IOException;
import java.net.URI;

import javax.xml.xpath.XPathExpression;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.faustedition.xml.XmlStore;


@Controller
@RequestMapping("/archive/")
public class ArchiveController {

	public static final URI ARCHIVES_DESCRIPTOR = URI.create("archives.xml");

	@Autowired
	private XmlStore xmlStore;

	@RequestMapping("{id}")
	public String archive(@PathVariable("id") String id, ModelMap model) throws IOException {
		XPathExpression xpathById = xpath("/f:archives/f:archive[@id='" + id + "']");
		Element archive = singleResult(xpathById, archives(), Element.class);
		if (archive == null) {
			throw new DataRetrievalFailureException(id);
		}
		model.addAttribute("archive", archive);
		return "document/archive";
	}

	@RequestMapping
	public String index(ModelMap model) throws IOException {
		model.put("archives", archives().getDocumentElement());
		return "document/archives";
	}

	protected Document archives() throws IOException {
		return xmlStore.get(ARCHIVES_DESCRIPTOR);
	}
}
