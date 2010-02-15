package de.swkk.metadata;

import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class ParalipomenaMapping extends AllegroRecordSet {
	private static final Resource METADATA_RESOURCE = new ClassPathResource("paralipomena-mapping.txt",
			ParalipomenaMapping.class);

	public static ParalipomenaMapping parse() throws IOException {
		ParalipomenaMapping pm = new ParalipomenaMapping();
		pm.parse(METADATA_RESOURCE);
		return pm;
	}

	private ParalipomenaMapping() {
	}

}
