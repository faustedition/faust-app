package de.faustedition.web.project;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ProjectController
{

	@RequestMapping("/project/about")
	public void about()
	{
	}

	@RequestMapping("/project/contact")
	public void contact()
	{
	}

	@RequestMapping("/project/imprint")
	public void imprint()
	{
	}

}
