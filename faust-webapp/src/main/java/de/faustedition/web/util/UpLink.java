package de.faustedition.web.util;

import org.apache.wicket.Page;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

public class UpLink extends Panel
{

	public UpLink(String id, Link<? extends Page> link)
	{
		super(id);
		add(link);
		link.add(new Image("upImage", new ResourceReference(UpLink.class, "ArrowUp.png"))
		{
			@Override
			protected boolean getStatelessHint()
			{
				return true;
			}
		});
	}

}
