package de.faustedition.metadata;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class ParalipomenaMapping extends AllegroRecord.Set {

	private static final long serialVersionUID = -4557547564928295864L;

	public ParalipomenaMapping() {
		setSource(new ClassPathResource("gsa-paralipomena-mapping.txt", ParalipomenaMapping.class));
	}

}
