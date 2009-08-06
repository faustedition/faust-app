package de.faustedition.model.transcription;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Repository extends TranscriptionStoreContents {
	public Repository(TranscriptionStore store, String name) {
		super(store, null, name);
	}

	public Collection<Portfolio> findPortfolios() throws TranscriptionStoreException {
		String[] portfolioNames = store.findCollections(getPath());
		List<Portfolio> portfolios = new ArrayList<Portfolio>(portfolioNames.length);
		for (String portfolioName : portfolioNames) {
			portfolios.add(new Portfolio(store, this, portfolioName));
		}
		return portfolios;
	}
}
