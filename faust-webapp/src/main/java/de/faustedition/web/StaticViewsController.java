package de.faustedition.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class StaticViewsController {

	@RequestMapping("/index.html")
	public String index() {
		return "index";
	}

	@RequestMapping("/contact.html")
	public String contact() {
		return "contact";
	}

	@RequestMapping("/imprint.html")
	public String imprint() {
		return "imprint";
	}
}
