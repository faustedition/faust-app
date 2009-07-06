package de.swkk.faustedition;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeSet;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CategoryResolver extends AllegroRecordSet {

	private static final long serialVersionUID = -7759853449154287205L;

	@Override
	public Resource getRecordSetResource() {
		return new ClassPathResource("/weimar_manuscripts_category_desc.txt");
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
