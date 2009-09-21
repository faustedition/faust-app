package de.faustedition.web;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.WebPage;

import de.faustedition.web.manuscript.ManuscriptsPage;

public class LoginPage extends WebPage
{

	public LoginPage()
	{
		super();
	}

	@Override
	protected void onBeforeRender()
	{
		RequestCycle.get().setRedirect(true);
		throw new RestartResponseException(ManuscriptsPage.class);
	}
}
