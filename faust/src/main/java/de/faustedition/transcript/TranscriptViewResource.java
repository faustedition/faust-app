package de.faustedition.transcript;

import java.io.IOException;

import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import de.faustedition.document.Archive;
import de.faustedition.document.MaterialUnit.Type;

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
