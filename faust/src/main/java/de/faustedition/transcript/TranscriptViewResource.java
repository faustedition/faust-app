package de.faustedition.transcript;

import de.faustedition.document.Archive;
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
		final Archive archive = document.getArchive();
		return templateFactory.create("transcript", getRequest().getClientInfo(), new ModelMap()
			.addAttribute("id", document.node.getId())
			.addAttribute("archiveName", (archive == null ? null : archive.getName()))
			.addAttribute("archiveId", (archive == null ? null : archive.getId()))
			.addAttribute("waId", document.getMetadataValue("wa-id"))
			.addAttribute("callnumber", document.getMetadataValue("callnumber")));
	}
}
