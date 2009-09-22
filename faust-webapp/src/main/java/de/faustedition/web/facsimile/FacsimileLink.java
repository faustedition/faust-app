package de.faustedition.web.facsimile;

import org.apache.wicket.markup.html.link.Link;

import de.faustedition.model.manuscript.Facsimile;
import de.faustedition.model.manuscript.FacsimileImageResolution;

public class FacsimileLink extends Link<Facsimile>
{

	private final Facsimile facsimile;
	private final FacsimileImageResolution resolution;

	public FacsimileLink(String id, Facsimile facsimile, FacsimileImageResolution resolution)
	{
		super(id);
		this.facsimile = facsimile;
		this.resolution = resolution;
	}

	@Override
	protected boolean getStatelessHint()
	{
		return true;
	}

	@Override
	protected CharSequence getURL()
	{
		return FacsimileImage.getURL(facsimile, resolution);
	}

	@Override
	public void onClick()
	{
	}

}
