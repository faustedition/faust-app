package de.faustedition.web.metadata;

import java.util.Collection;
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

import de.faustedition.model.metadata.MetadataAssignment;
import de.faustedition.model.metadata.MetadataFieldDefinition;
import de.faustedition.model.metadata.MetadataFieldGroup;

public class MetadataTable extends WebComponent {
	private transient Collection<MetadataAssignment> metadata;

	public MetadataTable(String id, Collection<MetadataAssignment> metadata) {
		super(id, Model.of(metadata));
		this.metadata = metadata;
		setVisible(!metadata.isEmpty());
	}

	@Override
	protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
		StringBuilder markup = new StringBuilder();
		Localizer localizer = Application.get().getResourceSettings().getLocalizer();
		
		SortedMap<MetadataFieldGroup, SortedSet<MetadataAssignment>> structuredMetadata = MetadataFieldDefinition.createStructuredMetadata(metadata);
		for (MetadataFieldGroup fieldGroup : structuredMetadata.keySet()) {

			CharSequence groupLabel = Strings.escapeMarkup(localizer.getString("metadata_group." + fieldGroup.toString(), this));
			markup.append(String.format("<tr><th colspan=\"2\" class=\"left small-caps secondary-color\">%s</th></tr>", groupLabel));

			for (MetadataAssignment value : structuredMetadata.get(fieldGroup)) {
				markup.append("<tr class=\"border\">");
				CharSequence fieldLabel = Strings.escapeMarkup(localizer.getString("metadata." + value.getField(), this));
				markup.append(String.format("<th class=\"right\" style=\"width: 40%%\">%s:</th>", fieldLabel));
				markup.append(String.format("<td style=\"width: 60%%\">%s</td>", Strings.replaceAll(Strings.escapeMarkup(value.getValue()), "\n", "<br/>")));

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
