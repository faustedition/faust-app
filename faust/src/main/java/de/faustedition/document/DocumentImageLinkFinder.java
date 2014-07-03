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

package de.faustedition.document;

import org.restlet.resource.ServerResource;
import org.springframework.stereotype.Component;

import java.util.Deque;

@Component
public class DocumentImageLinkFinder extends AbstractDocumentFinder {

	@Override
	protected ServerResource getResource(Document document, Deque<String> postfix, Deque<String> path) {
		DocumentImageLinkResource resource = applicationContext.getBean(DocumentImageLinkResource.class);
		if (postfix.size() != 1)
			return null;
		else {
			int pageNum;
			try {
				pageNum = Integer.parseInt(postfix.getFirst());
			} catch (NumberFormatException e) {
				return null;
			}
			resource.setDocument(document, pageNum);
		}
		return resource;
	}

}
