package de.faustedition.web.metadata;

import java.util.Collection;
import java.util.Locale;
import java.util.SortedMap;
import java.util.SortedSet;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.web.util.HtmlUtils;

import de.faustedition.model.metadata.MetadataAssignment;
import de.faustedition.model.metadata.MetadataFieldDefinition;
import de.faustedition.model.metadata.MetadataFieldGroup;

public class MetadataTable
{
	private final Collection<MetadataAssignment> metadata;
	private final MessageSource messageSource;
	private final Locale locale;

	public MetadataTable(Collection<MetadataAssignment> metadata, MessageSource messageSource, Locale locale)
	{
		this.metadata = metadata;
		this.messageSource = messageSource;
		this.locale = locale;
	}

	public boolean isEmpty()
	{
		return metadata.isEmpty();
	}

	public String toHtmlContent()
	{
		StringBuilder markup = new StringBuilder();
		SortedMap<MetadataFieldGroup, SortedSet<MetadataAssignment>> structuredMetadata = MetadataFieldDefinition.createStructuredMetadata(metadata);
		for (MetadataFieldGroup fieldGroup : structuredMetadata.keySet())
		{

			String groupLabel = HtmlUtils.htmlEscape(messageSource.getMessage("metadata_group." + fieldGroup.toString().toLowerCase(), null, locale));
			markup.append(String.format("<tr><th colspan=\"2\" class=\"left small-caps secondary-color\">%s</th></tr>", groupLabel));

			for (MetadataAssignment value : structuredMetadata.get(fieldGroup))
			{
				markup.append("<tr class=\"border\">");
				String fieldLabel = HtmlUtils.htmlEscape(messageSource.getMessage("metadata." + value.getField(), null, locale));
				markup.append(String.format("<th class=\"right\" style=\"width: 40%%\">%s:</th>", fieldLabel));
				markup.append(String.format("<td style=\"width: 60%%\">%s</td>", StringUtils.replace(HtmlUtils.htmlEscape(value.getValue()), "\n", "<br/>")));

				markup.append("</tr>");
			}

		}
		return markup.toString();
	}
}
