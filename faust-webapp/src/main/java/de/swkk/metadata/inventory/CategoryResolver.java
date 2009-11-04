package de.swkk.metadata.inventory;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeSet;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import de.swkk.metadata.AllegroRecordSet;

public class CategoryResolver extends AllegroRecordSet {
	private static final Resource RESOLVER_RESOURCE = new ClassPathResource("/data/weimar_manuscripts_category_desc.txt");

	public CategoryResolver() throws IOException {
		parse(RESOLVER_RESOURCE);
	}

	public List<String> getCategories() {
		if (isEmpty()) {
			return Collections.emptyList();
		}

		return new LinkedList<String>(new TreeSet<String>(first().values()));
	}

	public String resolve(String identifier) {
		identifier = StringUtils.trimWhitespace(identifier);

		for (SortedMap<String, String> record : this) {
			if (record.containsKey(identifier)) {
				return record.get(identifier);
			}
		}

		return null;
	}
}
