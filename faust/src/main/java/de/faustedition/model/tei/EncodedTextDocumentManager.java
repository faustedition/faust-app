package de.faustedition.model.tei;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

@Service
public class EncodedTextDocumentManager {
	@Value("#{config['base.url']}")
	private String baseUrl;

	public List<EncodedTextDocumentProcessor> defaultProcessors;

	@PostConstruct
	public void init() {
		defaultProcessors = Lists.newArrayList(new ProcessingInstructionProcessor(baseUrl),
				new NamespaceDeclarationProcessor(), new HeaderTemplateProcessor(), new HandDeclarationProcessor(),
				new CharacterDeclarationProcessor());
	}

	public EncodedTextDocument process(EncodedTextDocument teiDocument, EncodedTextDocumentProcessor... additionalProcessors) {
		for (EncodedTextDocumentProcessor processor : defaultProcessors) {
			processor.process(teiDocument);
		}

		for (EncodedTextDocumentProcessor processor : additionalProcessors) {
			processor.process(teiDocument);
		}

		return teiDocument;
	}

	public EncodedTextDocument create(EncodedTextDocumentProcessor... additionalProcessors) {
		return process(EncodedTextDocument.create(), additionalProcessors);
	}
}
