package de.faustedition.web;

import static de.faustedition.tei.EncodedTextDocument.xpath;
import static de.faustedition.xml.NodeListIterable.singleResult;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FilenameUtils;
import org.lmnl.lom.base.DefaultLmnlDocument;
import org.lmnl.xml.PlainTextXmlFilter;
import org.lmnl.xml.SaxBasedLmnlBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.collect.Sets;

import de.faustedition.facsimile.Facsimile;
import de.faustedition.tei.EncodedTextDocument;
import de.faustedition.xml.XmlStore;

@Controller
public class WitnessController implements InitializingBean {
	private static final Resource TEI_2_HTML_XSL_RESOURCE = new ClassPathResource("tei-2-xhtml.xsl", WitnessController.class);
	public static final String WITNESS_VIEW_NAME = "witness/witness";
	private Templates tei2HtmlTemplates;

	@Autowired
	private XmlStore xmlStore;

	@RequestMapping(value = "/Witness/**", headers = { "Accept=application/json" })
	public void witnessModel(HttpServletRequest request, HttpServletResponse response) throws IOException, SAXException, URISyntaxException {
		String path = ControllerUtil.getPath(request, null);
		if (!"xml".equals(FilenameUtils.getExtension(path))) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, path);
			return;
		}

		SaxBasedLmnlBuilder builder = new SaxBasedLmnlBuilder(new PlainTextXmlFilter()//
				.withLineElements(Sets.newHashSet("pb", "lb", "zone", "line")));
		DefaultLmnlDocument document = new DefaultLmnlDocument(new URI("witness", path, null));
		EncodedTextDocument witness = new EncodedTextDocument(xmlStore.get(URI.create(path)));
	}

	@RequestMapping(value = "/Witness/**")
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

	private void displayCollection(ModelAndView mv, String path) throws IOException {
		mv.setViewName("witness/collection");
		mv.addObject("contents", xmlStore.list(URI.create(path)));
	}

	private void displayWitness(ModelAndView mv, String path) throws Exception {
		EncodedTextDocument document = new EncodedTextDocument(xmlStore.get(URI.create(path)));
		mv.addObject("document", document);
		Element facsimile = singleResult(xpath("//tei:facsimile/tei:graphic"), document.getDom(), Element.class);
		if (facsimile != null) {
			mv.addObject("facsimile", Facsimile.fromUri(URI.create(facsimile.getAttribute("url"))));
		}
		StringWriter htmlWriter = new StringWriter();
		tei2HtmlTemplates.newTransformer().transform(new DOMSource(document.getDom()), new StreamResult(htmlWriter));
		mv.addObject("htmlTranscription", htmlWriter.toString());
		mv.setViewName(WITNESS_VIEW_NAME);
	}

	public void afterPropertiesSet() throws Exception {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		tei2HtmlTemplates = transformerFactory.newTemplates(new StreamSource(TEI_2_HTML_XSL_RESOURCE.getInputStream()));
	}
}
