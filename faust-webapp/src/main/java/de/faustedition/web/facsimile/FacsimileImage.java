package de.faustedition.web.facsimile;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;

import de.faustedition.model.manuscript.Facsimile;
import de.faustedition.model.manuscript.FacsimileImageResolution;

public class FacsimileImage extends WebComponent
{

	private final String path;
	private final FacsimileImageResolution resolution;

	public FacsimileImage(String id, Facsimile facsimile, FacsimileImageResolution resolution)
	{
		super(id);
		this.path = facsimile.getImagePath();
		this.resolution = resolution;
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);

		RequestCycle requestCycle = RequestCycle.get();
		CharSequence facsimileUrl = FacsimileController.URL_PREFIX + "/" + path + resolution.getSuffix();
		facsimileUrl = requestCycle.getOriginalResponse().encodeURL(facsimileUrl);
		facsimileUrl = requestCycle.getRequest().getRelativePathPrefixToContextRoot() + facsimileUrl;
		tag.put("src", facsimileUrl);
	}
}
