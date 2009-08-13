package de.faustedition.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class LoginController {

	@RequestMapping("/project/login")
	public ModelAndView login() {
		return new ModelAndView(new RedirectView("/manuscripts/", true));
	}
}
