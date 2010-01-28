package de.swkk.metadata.inventory;

import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import de.swkk.metadata.AllegroRecordSet;

public class ParalipomenaMetadata extends AllegroRecordSet {
	private static final Resource METADATA_RESOURCE = new ClassPathResource("/data/weimar_manuscripts_paralipomena.txt");

	public static ParalipomenaMetadata parse() throws IOException {
		ParalipomenaMetadata pm = new ParalipomenaMetadata();
		pm.parse(METADATA_RESOURCE);
		return pm;
	}

	private ParalipomenaMetadata() {
	}

}
