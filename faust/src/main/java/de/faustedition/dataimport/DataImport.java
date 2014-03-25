/*
 * Copyright (c) 2014 Faust Edition development team.
 *
 * This file is part of the Faust Edition.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.faustedition.dataimport;

import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;

import de.faustedition.FaustURI;
import de.faustedition.Runtime;
import de.faustedition.genesis.GeneticRelationManager;
import de.faustedition.genesis.MacrogeneticRelationManager;
import de.faustedition.text.TextManager;
import de.faustedition.transcript.GoddagTranscriptManager;

@Component
public class DataImport extends Runtime implements Runnable {

	@Autowired
	private GoddagTranscriptManager transcriptManager;

	@Autowired
	private TextManager textManager;

	@Autowired
	private Logger logger;

	@Autowired
	private GeneticRelationManager geneticRelationManager;

	@Autowired
	private MacrogeneticRelationManager macrogeneticRelationManager;

	public static void main(String[] args) throws Exception {
		try {
			main(DataImport.class, args);
		} finally {
			System.exit(0);
		}
	}

	@Override
	public void run() {
		logger.info("Importing all data into graph");
		final SortedSet<FaustURI> failed = new TreeSet<FaustURI>();
		final long startTime = System.currentTimeMillis();

		failed.addAll(transcriptManager.feedGraph());
		failed.addAll(macrogeneticRelationManager.feedGraph());
		failed.addAll(textManager.feedGraph());
		//failed.addAll(textManager.feedDatabase());
		geneticRelationManager.feedGraph();


		logger.info(String.format("Import finished in %.3f seconds", (System.currentTimeMillis() - startTime) / 1000.0f));
		if (!failed.isEmpty()) {
			logger.error("Failed imports:\n" + Joiner.on("\n").join(failed));
		}
	}
}
