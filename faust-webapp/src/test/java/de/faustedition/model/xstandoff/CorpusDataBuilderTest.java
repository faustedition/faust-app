package de.faustedition.model.xstandoff;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.faustedition.model.tei.TEIDocument;
import de.faustedition.util.XMLUtil;

public class CorpusDataBuilderTest
{
	private static final String[] TEST_RESOURCE_PATHS = new String[] { "/xstandoff/391098_0349.xml", "/xstandoff/391098_0360.xml", "/xstandoff/391098_0377.xml" };

	@Test
	public void buildCorpusData() throws Exception
	{
		for (Node testResourceNode : testResourceNodes())
		{
			Document document = XMLUtil.documentBuilder().newDocument();
			Map<String, String> namespaces = new HashMap<String, String>();
			namespaces.put(TEIDocument.TEI_NS_URI, "");
			new CorpusDataBuilder().build(testResourceNode).serialize(document, namespaces);
			XMLUtil.serialize(document, System.out, true);
		}
	}

	@Test
	public void extractLayer() throws Exception
	{
		for (Node testResourceNode : testResourceNodes())
		{
			CorpusData corpusData = new CorpusDataBuilder().build(testResourceNode);
			AnnotationLevel annotationLevel = corpusData.getAnnotationLevels().get(0);

			AnnotationLayer source = (AnnotationLayer) annotationLevel.getChildren().get(0);
			AnnotationLayer destination = new AnnotationLayer(corpusData, annotationLevel);
			annotationLevel.add(destination);

			(new AnnotationLayerExtractor(source, destination)
			{
				private final Set<String> EDITING_ELEMENTS = Sets.newHashSet("add", "del", "subst");

				@Override
				public AnnotationElement transform(AnnotationElement element)
				{
					AnnotationElement transformed = (AnnotationElement) element.copy();
					for (AnnotationAttribute attr : element.getChildrenOfType(AnnotationAttribute.class))
					{
						transformed.add(attr.copy());
					}
					return transformed;
				}

				@Override
				public boolean extract(AnnotationElement element)
				{
					return TEIDocument.TEI_NS_URI.equals(element.getNamespace()) && EDITING_ELEMENTS.contains(element.getLocalName());
				}
			}).extract();

			Map<String, String> namespaces = new HashMap<String, String>();
			namespaces.put(TEIDocument.TEI_NS_URI, "");
			Document document = XMLUtil.documentBuilder().newDocument();
			corpusData.serialize(document, namespaces);
			XMLUtil.serialize(document, System.out, true);
		}
	}

	private static List<Node> testResourceNodes() throws Exception
	{
		Transformer cleanupTransformer = XMLUtil.newTransformer(new StreamSource(CorpusDataBuilder.class.getResourceAsStream("/xsl/text-tei-cleanup.xsl")));

		List<Node> testResourceNodes = Lists.newArrayList();
		for (String testResourcePath : TEST_RESOURCE_PATHS)
		{
			DOMResult cleanedUpResult = new DOMResult();
			cleanupTransformer.transform(new DOMSource(XMLUtil.parse(CorpusDataBuilderTest.class.getResourceAsStream(testResourcePath))), cleanedUpResult);
			Element textElement = new TEIDocument(cleanedUpResult.getNode()).getTextElement();
			XMLUtil.serialize(textElement, System.out, false);
			testResourceNodes.add(textElement);
		}
		return testResourceNodes;
	}
}
