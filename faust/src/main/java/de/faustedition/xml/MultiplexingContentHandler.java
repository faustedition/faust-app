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

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class MultiplexingContentHandler implements ContentHandler {

	private final ContentHandler[] handlers;

	public MultiplexingContentHandler(ContentHandler... handlers) {
		this.handlers = handlers;
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		for (ContentHandler handler : handlers) {
			handler.setDocumentLocator(locator);
		}
	}

	@Override
	public void startDocument() throws SAXException {
		for (ContentHandler handler : handlers) {
			handler.startDocument();
		}
	}

	@Override
	public void endDocument() throws SAXException {
		for (ContentHandler handler : handlers) {
			handler.endDocument();
		}
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		for (ContentHandler handler : handlers) {
			handler.startPrefixMapping(prefix, uri);
		}
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		for (ContentHandler handler : handlers) {
			handler.endPrefixMapping(prefix);
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		for (ContentHandler handler : handlers) {
			handler.startElement(uri, localName, qName, atts);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		for (ContentHandler handler : handlers) {
			handler.endElement(uri, localName, qName);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		for (ContentHandler handler : handlers) {
			handler.characters(ch, start, length);
		}
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		for (ContentHandler handler : handlers) {
			handler.ignorableWhitespace(ch, start, length);
		}
	}

	@Override
	public void processingInstruction(String target, String data) throws SAXException {
		for (ContentHandler handler : handlers) {
			handler.processingInstruction(target, data);
		}
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		for (ContentHandler handler : handlers) {
			handler.skippedEntity(name);
		}
	}
}
