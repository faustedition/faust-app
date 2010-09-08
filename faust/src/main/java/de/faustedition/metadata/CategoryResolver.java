package de.faustedition.metadata;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeSet;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CategoryResolver extends AllegroRecord.Set {
	private static final long serialVersionUID = -7759853449154287205L;

	public CategoryResolver() {
		setSource(new ClassPathResource("gsa-category-descriptions.txt", CategoryResolver.class));
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
