package de.faustedition.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RedirectController
{

	@RequestMapping("/")
	public String homepage()
	{
		return "redirect:project/about";
	}

	@RequestMapping("/login")
	public String login()
	{
		return "redirect:manuscripts/";
	}
}
