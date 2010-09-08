package de.faustedition.metadata;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class WaPrintMapping extends AllegroRecord.Set {

	private static final long serialVersionUID = 2910010206714105914L;
	private static final String PAGE_FIELD = "55a";
	private static final String DESCRIPTION_FIELD = "222";
	private static final String VOLUME_FIELD = "445";

	private Map<String, Integer> registerToHierarchyMapping = new HashMap<String, Integer>();
	private Integer unregisteredFile;

	public WaPrintMapping() {
		setSource(new ClassPathResource("gsa-wa-print-mapping.txt", WaPrintMapping.class));
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
		return (description == null || !registerToHierarchyMapping.containsKey(description) ? unregisteredFile
				: registerToHierarchyMapping.get(description));
	}
}
