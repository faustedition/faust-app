package de.faustedition.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class StaticViewsController {

	@RequestMapping("/about")
	public String index() {
		return "about";
	}

	@RequestMapping("/contact")
	public String contact() {
		return "contact";
	}

	@RequestMapping("/imprint")
	public String imprint() {
		return "imprint";
	}

	@RequestMapping("/internal")
	public String internal() {
		return "internal";
	}
}
