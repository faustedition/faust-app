package de.faustedition.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TextController
{
	@RequestMapping("/text/")
	public String sample()
	{
		return "text/sample";
	}
}
