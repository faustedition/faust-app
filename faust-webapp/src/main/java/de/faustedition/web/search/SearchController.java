package de.faustedition.web.search;

import org.compass.core.Compass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SearchController
{
	@Autowired
	private Compass compass;

	@RequestMapping("/search")
	public void search(@ModelAttribute("searchCommand") SearchCommand searchCommand, ModelMap model)
	{
		model.addAttribute(searchCommand.execute(compass));
	}
}
