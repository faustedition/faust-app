package de.faustedition.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ProjectViewsController {

	@RequestMapping("/project/about")
	public String index() {
		return "project/about";
	}

	@RequestMapping("/project/contact")
	public String contact() {
		return "project/contact";
	}

	@RequestMapping("/project/imprint")
	public String imprint() {
		return "project/imprint";
	}
}
