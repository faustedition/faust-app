package de.swkk.metadata.inventory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import de.swkk.metadata.AllegroRecord;
import de.swkk.metadata.AllegroRecordSet;

public class WeimarerAusgabeFaustRegister extends AllegroRecordSet {
	private static final String PAGE_FIELD = "55a";
	private static final String DESCRIPTION_FIELD = "222";
	private static final String VOLUME_FIELD = "445";
	private static final Resource FAUST_REGISTER_RESOURCE = new ClassPathResource("/weimar_manuscripts_wa_druck.txt");
	private Map<String, Integer> registerToHierarchyMapping = new HashMap<String, Integer>();
	private Integer unregisteredFile;

	public WeimarerAusgabeFaustRegister() throws IOException {
		parse(FAUST_REGISTER_RESOURCE);
	}

	public SortedSet<String> getDescriptionSet() {
		SortedSet<String> descriptionSet = new TreeSet<String>();
		for (AllegroRecord record : this) {
			if (record.containsKey(DESCRIPTION_FIELD)) {
				descriptionSet.add(record.get(DESCRIPTION_FIELD));
			}
		}
		return descriptionSet;
	}

	public String resolve(String reference) {
		String[] referenceComponents = StringUtils.stripAll(reference.split(","));
		if (referenceComponents.length < 2) {
			return null;
		}

		for (AllegroRecord record : this) {
			String volume = record.get(VOLUME_FIELD);
			String page = record.get(PAGE_FIELD);
			String description = record.get(DESCRIPTION_FIELD);

			if (volume == null || page == null || description == null) {
				continue;
			}

			if (volume.equalsIgnoreCase(referenceComponents[0]) && page.equalsIgnoreCase(referenceComponents[1])) {
				return description;
			}
		}

		return null;
	}

	public Integer resolveMetadataNode(String description) {
		return (description == null || !registerToHierarchyMapping.containsKey(description) ? unregisteredFile : registerToHierarchyMapping.get(description));
	}
}
