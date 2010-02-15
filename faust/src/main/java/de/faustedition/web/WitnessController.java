package de.faustedition.web;

import static de.faustedition.tei.EncodedTextDocument.xpath;
import static de.faustedition.xml.NodeListIterable.singleResult;

import java.io.StringWriter;
import java.net.URI;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Element;

import de.faustedition.facsimile.FacsimileReference;
import de.faustedition.tei.EncodedTextDocument;
import de.faustedition.xml.XmlDbManager;

@Controller
public class WitnessController {
	private static final Resource TEI_2_HTML_XSL_RESOURCE = new ClassPathResource("tei-2-xhtml.xsl", WitnessController.class);
	private Templates tei2HtmlTemplates;

	@Autowired
	private XmlDbManager xmlDbManager;

	@PostConstruct
	public void init() throws Exception {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		tei2HtmlTemplates = transformerFactory.newTemplates(new StreamSource(TEI_2_HTML_XSL_RESOURCE.getInputStream()));
	}

	@RequestMapping("/Witness/**")
	public ModelAndView display(HttpServletRequest request) throws Exception {
		ModelAndView mv = new ModelAndView();
		String path = ControllerUtil.getPath(request, null);

		if ("xml".equals(FilenameUtils.getExtension(path))) {
			displayWitness(mv, path);
		} else {
			if (!path.endsWith("/")) {
				path += "/";
			}
			displayCollection(mv, path);
		}

		mv.addObject("path", path);
		return mv;
	}

	private void displayCollection(ModelAndView mv, String path) {
		mv.setViewName("witness/collection");
		mv.addObject("contents", xmlDbManager.contentsOf(URI.create(path)));
	}

	private void displayWitness(ModelAndView mv, String path) throws Exception {
		EncodedTextDocument document = new EncodedTextDocument(xmlDbManager.get(URI.create(path)));

		Element facsimile = singleResult(xpath("//tei:facsimile/tei:graphic"), document.getDom(), Element.class);
		if (facsimile != null) {
			mv.addObject("facsimile", FacsimileReference.fromURI(URI.create(facsimile.getAttribute("url"))));
		}

		StringWriter htmlWriter = new StringWriter();
		tei2HtmlTemplates.newTransformer().transform(new DOMSource(document.getDom()), new StreamResult(htmlWriter));

		mv.addObject("htmlTranscription", htmlWriter.toString());
		mv.setViewName("witness/witness");
	}
}
