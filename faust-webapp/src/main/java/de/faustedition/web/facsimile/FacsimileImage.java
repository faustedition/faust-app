package de.faustedition.web.facsimile;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;

import de.faustedition.model.manuscript.Facsimile;
import de.faustedition.model.manuscript.FacsimileImageResolution;

public class FacsimileImage extends WebComponent
{

	private final Facsimile facsimile;
	private final FacsimileImageResolution resolution;

	public FacsimileImage(String id, Facsimile facsimile, FacsimileImageResolution resolution)
	{
		super(id);
		this.facsimile = facsimile;
		this.resolution = resolution;
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		tag.put("src", getURL(facsimile, resolution));
	}

	protected static CharSequence getURL(Facsimile facsimile, FacsimileImageResolution resolution)
	{
		RequestCycle requestCycle = RequestCycle.get();
		CharSequence facsimileUrl = FacsimileController.URL_PREFIX + "/" + facsimile.getImagePath() + resolution.getSuffix();
		facsimileUrl = requestCycle.getOriginalResponse().encodeURL(facsimileUrl);
		return requestCycle.getRequest().getRelativePathPrefixToContextRoot() + facsimileUrl;
	}
}
