package de.faustedition.web;

import java.io.StringWriter;
import java.net.URI;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;

import de.faustedition.xml.XmlDbManager;
import de.faustedition.xml.XmlUtil;

@Controller
public class WitnessController {

	@Autowired
	private XmlDbManager xmlDbManager;
	
	@RequestMapping("/Witness/**")
	public ModelAndView display(HttpServletRequest request) {
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

	private void displayWitness(ModelAndView mv, String path) {
		Document document = xmlDbManager.get(URI.create(path));
		StringWriter documentWriter = new StringWriter();
		XmlUtil.serialize(document, documentWriter);
		
		mv.addObject("document", documentWriter.toString());
		mv.setViewName("witness/witness");
	}
}
