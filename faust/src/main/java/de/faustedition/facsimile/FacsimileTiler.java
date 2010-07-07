package de.faustedition.facsimile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.faustedition.Log;

@Service
public class FacsimileTiler implements Runnable {

	@Autowired
	private FacsimileStore store;

	@Override
	public void run() {
		Log.LOGGER.debug("Building complete facsimile tile store");

		for (Facsimile f : store) {
			store.tiles(f);
		}
	}

}
