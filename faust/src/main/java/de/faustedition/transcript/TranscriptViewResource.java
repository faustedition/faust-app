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

import de.faustedition.document.Archive;
import de.faustedition.document.MaterialUnit.Type;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import java.io.IOException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TranscriptViewResource extends TranscriptResource {

	@Get("html")
	public Representation page() throws IOException {
		//FIXME when requesting a transcript of a non-document, we loop endlessly in the graph
		//in getArchive()!
		if (materialUnit.getType() == Type.ARCHIVALDOCUMENT)
		{
			final Archive archive = materialUnit.getArchive();
			return templateFactory.create("transcript", getRequest().getClientInfo(), new ModelMap()
			.addAttribute("id", materialUnit.node.getId())
			.addAttribute("archiveName", (archive == null ? null : archive.getName()))
			.addAttribute("archiveId", (archive == null ? null : archive.getId()))
			.addAttribute("waId", materialUnit.getMetadataValue("wa-id"))
			.addAttribute("callnumber", materialUnit.getMetadataValue("callnumber")));
		} else {
			throw new RuntimeException("Only text transcripts can be displayed!");
		}
	}
}
