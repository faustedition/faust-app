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

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface Namespaces {
	String FAUST_NS_URI = "http://www.faustedition.net/ns";
	String FAUST_NS_PREFIX = "f";

	String TEI_NS_URI = "http://www.tei-c.org/ns/1.0";
	String TEI_NS_PREFIX = "tei";

	String TEI_SIG_GE_URI = "http://www.tei-c.org/ns/geneticEditions";
	String TEI_SIG_GE_PREFIX = "ge";
	URI TEI_SIG_GE = URI.create(TEI_SIG_GE_URI);

	String SVG_NS_URI = "http://www.w3.org/2000/svg";
	String SVG_NS_PREFIX = "svg";

	String XLINK_NS_URI = "http://www.w3.org/1999/xlink";
	String XLINK_NS_PREFIX = "xlink";
	
	String XML_NS_URI = "http://www.w3.org/XML/1998/namespace";
	String XML_NS_PREFIX= "xml";
	
}
