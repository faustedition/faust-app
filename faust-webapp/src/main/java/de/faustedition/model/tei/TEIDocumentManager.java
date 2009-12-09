package de.faustedition.model.tei;

import java.util.List;

import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

@Service
public class TEIDocumentManager
{
	public List<TEIDocumentProcessor> defaultProcessors = Lists.newArrayList(new NamespaceDeclarationProcessor(), new HeaderTemplateProcessor(), new HandDeclarationProcessor(),
			new CharacterDeclarationProcessor());

	public TEIDocument process(TEIDocument teiDocument, TEIDocumentProcessor... additionalProcessors)
	{
		for (TEIDocumentProcessor processor : defaultProcessors)
		{
			processor.process(teiDocument);
		}

		for (TEIDocumentProcessor processor : additionalProcessors)
		{
			processor.process(teiDocument);
		}

		return teiDocument;
	}

	public TEIDocument create(TEIDocumentProcessor... additionalProcessors)
	{
		return process(new TEIDocument(), additionalProcessors);
	}
}
