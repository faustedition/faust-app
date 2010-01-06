package de.faustedition.model.tei;

import java.util.List;

import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

@Service
public class EncodedDocumentManager
{
	public List<EncodedDocumentProcessor> defaultProcessors = Lists.newArrayList(new NamespaceDeclarationProcessor(), new HeaderTemplateProcessor(), new HandDeclarationProcessor(),
			new CharacterDeclarationProcessor());

	public EncodedDocument process(EncodedDocument teiDocument, EncodedDocumentProcessor... additionalProcessors)
	{
		for (EncodedDocumentProcessor processor : defaultProcessors)
		{
			processor.process(teiDocument);
		}

		for (EncodedDocumentProcessor processor : additionalProcessors)
		{
			processor.process(teiDocument);
		}

		return teiDocument;
	}

	public EncodedDocument create(EncodedDocumentProcessor... additionalProcessors)
	{
		return process(EncodedDocument.create(), additionalProcessors);
	}
}
