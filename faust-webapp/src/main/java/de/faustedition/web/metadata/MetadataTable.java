package de.faustedition.web.metadata;

import java.util.SortedMap;
import java.util.SortedSet;

import org.apache.wicket.Application;
import org.apache.wicket.Localizer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;

import de.faustedition.model.metadata.MetadataBundle;
import de.faustedition.model.metadata.MetadataFieldGroup;
import de.faustedition.model.metadata.MetadataValue;

public class MetadataTable extends WebComponent {
	private MetadataBundle metadata;

	public MetadataTable(String id, MetadataBundle metadata) {
		super(id, new Model<MetadataBundle>(metadata));
		this.metadata = metadata;
		setVisible(!metadata.getValues().isEmpty());
	}

	@Override
	protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
		StringBuilder markup = new StringBuilder();
		Localizer localizer = Application.get().getResourceSettings().getLocalizer();
		
		SortedMap<MetadataFieldGroup, SortedSet<MetadataValue>> structuredMetadata = metadata.getStructuredMetadata();
		for (MetadataFieldGroup fieldGroup : structuredMetadata.keySet()) {

			CharSequence groupLabel = Strings.escapeMarkup(localizer.getString("metadata_group." + fieldGroup.toString(), this));
			markup.append(String.format("<tr><th colspan=\"2\" class=\"left small-caps secondary-color\">%s</th></tr>", groupLabel));

			for (MetadataValue value : structuredMetadata.get(fieldGroup)) {
				markup.append("<tr class=\"horizontal-border\">");
				CharSequence fieldLabel = Strings.escapeMarkup(localizer.getString("metadata." + value.getField(), this));
				markup.append(String.format("<th class=\"right\" style=\"width: 30%%\">%s:</th>", fieldLabel));
				markup.append(String.format("<td style=\"width: 70%%\">%s</td>", Strings.replaceAll(Strings.escapeMarkup(value.getValue()), "\n", "<br/>")));

				markup.append("</tr>");
			}
		}
		replaceComponentTagBody(markupStream, openTag, markup.toString());
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		tag.setType(XmlTag.OPEN);
	}
}
