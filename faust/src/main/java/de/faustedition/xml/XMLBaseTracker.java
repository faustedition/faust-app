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

package de.faustedition.xml;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import javax.xml.XMLConstants;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.base.Preconditions;

public class XMLBaseTracker extends DefaultHandler {
	private static final String NULL_BASE = "";
	private Deque<String> baseStack = new ArrayDeque<String>();
	private String base;

	public XMLBaseTracker(String initialBase) {
		if (initialBase != null) {
			baseStack.push(initialBase);
			base = initialBase;
		}
	}

	public String getBase() {
		return base;
	}

	public URI getBaseURI() {
		return URI.create(base);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		String newBase = attributes.getValue(XMLConstants.XML_NS_URI, "base");
		baseStack.push(newBase == null ? NULL_BASE : newBase);
		if (newBase != null) {
			base = newBase;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		String lastBase = baseStack.pop();
		if (!lastBase.equals(NULL_BASE)) {
			Preconditions.checkState(lastBase.equals(base));
			base = null;

			for (Iterator<String> it = baseStack.descendingIterator(); it.hasNext();) {
				base = it.next();
				if (!base.equals(NULL_BASE)) {
					break;
				}
			}

			if (base.equals(NULL_BASE)) {
				base = null;
			}
		}
	}
}
