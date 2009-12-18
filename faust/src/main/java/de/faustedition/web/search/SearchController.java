package de.faustedition.web.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import de.faustedition.model.search.SearchException;
import de.faustedition.model.search.SearchIndex;

@Controller
public class SearchController
{
	@Autowired
	private SearchIndex searchIndex;

	@RequestMapping("/search")
	public void search(@ModelAttribute("searchCommand") SearchCommand searchCommand, ModelMap model) throws SearchException
	{
		model.addAttribute(searchCommand.execute(searchIndex));
	}
}
