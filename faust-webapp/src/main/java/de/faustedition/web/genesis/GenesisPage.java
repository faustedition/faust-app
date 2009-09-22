package de.faustedition.web.genesis;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;

import com.google.common.collect.Lists;

import de.faustedition.util.ErrorUtil;
import de.faustedition.web.PageBase;
import de.faustedition.web.manuscript.ManuscriptPage;

public class GenesisPage extends PageBase
{
	private static final List<ParalipomenonReference> PARALIPOMENA_REFS = Lists.newArrayList(new ParalipomenonReference("P195", "391082", "0002"), new ParalipomenonReference("P21", "390782",
			"0002"), new ParalipomenonReference("P1", "390720", "0002"), new ParalipomenonReference("P93/P95", "390882", "0002"), new ParalipomenonReference("P91", "391314", "0002"),
			new ParalipomenonReference("P92a", "390781", "0002"), new ParalipomenonReference("P92b", "390826", "0002"), new ParalipomenonReference("P96", "390050", "0002"),
			new ParalipomenonReference("P97", "390777", "0002"), new ParalipomenonReference("P98a", "390705", "0002"), new ParalipomenonReference("P98b", "390705", "0003"));

	private static final ParalipomenonReference URFAUST_REF = new ParalipomenonReference("Urfaust-Schluss", "390028", "0095");

	public GenesisPage()
	{
		super();
		add(new WebComponent("lineAnalysis")
		{
			@Override
			protected void onComponentTag(ComponentTag tag)
			{
				super.onComponentTag(tag);
				tag.put("src", RequestCycle.get().getRequest().getRelativePathPrefixToContextRoot() + "chart/genesis.png");
			}
		});

		add(new WebComponent("lineAnalysisLinks")
		{
			@Override
			protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
			{
				try
				{
					StringWriter imageMap = new StringWriter();
					new GenesisExampleChart().render(new ByteArrayOutputStream(), new PrintWriter(imageMap), RequestCycle.get().getRequest().getRelativePathPrefixToContextRoot()
							+ "manuscripts/transcription/", "genesisChart");
					replaceComponentTagBody(markupStream, openTag, imageMap.toString());
				}
				catch (IOException e)
				{
					throw ErrorUtil.fatal("I/O exception while generating genesis chart image map", e);
				}
			}
		});
		add(new DataView<ParalipomenonReference>("paralipomena", new ListDataProvider<ParalipomenonReference>(PARALIPOMENA_REFS))
		{

			@Override
			protected void populateItem(Item<ParalipomenonReference> item)
			{
				ParalipomenonReference reference = item.getModelObject();

				PageParameters parameters = new PageParameters();
				parameters.put("0", "GSA");
				parameters.put("1", reference.portfolio);
				parameters.put("2", reference.manuscript);

				BookmarkablePageLink<ManuscriptPage> link = new BookmarkablePageLink<ManuscriptPage>("paraLink", ManuscriptPage.class, parameters);
				link.add(new Label("paraLinkTitle", reference.name));
				item.add(link);
			}

		});

		PageParameters parameters = new PageParameters();
		parameters.put("0", "GSA");
		parameters.put("1", URFAUST_REF.portfolio);
		parameters.put("2", URFAUST_REF.manuscript);

		BookmarkablePageLink<ManuscriptPage> urfaustLink = new BookmarkablePageLink<ManuscriptPage>("urfaustLink", ManuscriptPage.class, parameters);
		urfaustLink.add(new Label("urfaustTitle", URFAUST_REF.name));
		add(urfaustLink);
	}

	@Override
	public String getPageTitle()
	{
		return "Genese";
	}

	private static class ParalipomenonReference implements Serializable
	{
		private String name;
		private String portfolio;
		private String manuscript;

		private ParalipomenonReference(String name, String portfolio, String manuscript)
		{
			this.name = name;
			this.portfolio = portfolio;
			this.manuscript = manuscript;
		}
	}
}
