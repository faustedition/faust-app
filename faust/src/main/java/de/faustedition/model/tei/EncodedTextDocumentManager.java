package de.faustedition.model.tei;

import java.util.List;

import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

@Service
public class EncodedTextDocumentManager
{
	public List<EncodedTextDocumentProcessor> defaultProcessors = Lists.newArrayList(new NamespaceDeclarationProcessor(), new HeaderTemplateProcessor(), new HandDeclarationProcessor(),
			new CharacterDeclarationProcessor());

	public EncodedTextDocument process(EncodedTextDocument teiDocument, EncodedTextDocumentProcessor... additionalProcessors)
	{
		for (EncodedTextDocumentProcessor processor : defaultProcessors)
		{
			processor.process(teiDocument);
		}

		for (EncodedTextDocumentProcessor processor : additionalProcessors)
		{
			processor.process(teiDocument);
		}

		return teiDocument;
	}

	public EncodedTextDocument create(EncodedTextDocumentProcessor... additionalProcessors)
	{
		return process(EncodedTextDocument.create(), additionalProcessors);
	}
}
