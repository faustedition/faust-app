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

package de.faustedition.transcript;

import static de.faustedition.xml.Namespaces.TEI_NS_PREFIX;

import org.goddag4j.Element;
import org.goddag4j.token.LineTokenMarkupGenerator;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import de.faustedition.FaustURI;
import de.faustedition.graph.TokenizerUtil;

public class TextualGoddagTranscript extends GoddagTranscript {

	public TextualGoddagTranscript(Node node) {
		super(node);
	}

	public TextualGoddagTranscript(GraphDatabaseService db, FaustURI source, Element root) {
		super(db, TranscriptType.TEXTUAL, source, root);
	}

	public void postprocess() {
	}

	@Override
	public void tokenize() {
		super.tokenize();
		TokenizerUtil.tokenize(getTrees(), getDefaultRoot(), new LineTokenMarkupGenerator(), "f", "lines");
	}
	
	@Override
	public Element getDefaultRoot() {
		return getTrees().getRoot(TEI_NS_PREFIX, "text");
	}
}
