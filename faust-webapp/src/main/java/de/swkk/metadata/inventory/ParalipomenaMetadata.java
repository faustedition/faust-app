package de.swkk.metadata.inventory;

import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import de.swkk.metadata.AllegroRecordSet;

public class ParalipomenaMetadata extends AllegroRecordSet {
	private static final Resource METADATA_RESOURCE = new ClassPathResource("/weimar_manuscripts_paralipomena.txt");

	public ParalipomenaMetadata() throws IOException {
		parse(METADATA_RESOURCE);
	}
}
