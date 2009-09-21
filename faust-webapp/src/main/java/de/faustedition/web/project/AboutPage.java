package de.faustedition.web.project;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.image.Image;

import de.faustedition.web.PageBase;

public class AboutPage extends PageBase
{

	public AboutPage()
	{
		super();
		add(new Image("faustEmblem", new ResourceReference(AboutPage.class, "AboutPage_FaustEmblem.jpg")));
	}

	@Override
	public String getPageTitle()
	{
		return "Digitale Faust-Edition";
	}
}
