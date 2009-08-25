package de.faustedition.web.facsimile;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;

import de.faustedition.model.facsimile.FacsimileResolution;
import de.faustedition.model.transcription.Transcription;

public class FacsimileImage extends WebComponent {

	private final String path;
	private final FacsimileResolution resolution;

	public FacsimileImage(String id, Transcription transcription, FacsimileResolution resolution) {
		super(id);
		this.path = transcription.getPathInStore();
		this.resolution = resolution;
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);

		RequestCycle requestCycle = RequestCycle.get();
		CharSequence facsimileUrl = FacsimileController.URL_PREFIX + "/" + path + resolution.getSuffix();
		facsimileUrl = requestCycle.getOriginalResponse().encodeURL(facsimileUrl);
		facsimileUrl = requestCycle.getRequest().getRelativePathPrefixToContextRoot() + facsimileUrl;
		tag.put("src", facsimileUrl);
	}
}
