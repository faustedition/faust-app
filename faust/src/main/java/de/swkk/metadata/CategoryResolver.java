package de.swkk.metadata;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeSet;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

public class CategoryResolver extends AllegroRecordSet {
	private static final Resource RESOLVER_RESOURCE = new ClassPathResource("category-descriptions.txt", CategoryResolver.class);

	public static CategoryResolver parse() throws IOException {
		CategoryResolver cr = new CategoryResolver();
		cr.parse(RESOLVER_RESOURCE);
		return cr;
	}

	private CategoryResolver() {
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
