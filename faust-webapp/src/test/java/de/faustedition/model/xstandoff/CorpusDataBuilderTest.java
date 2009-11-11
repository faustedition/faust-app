package de.faustedition.model.xstandoff;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import com.google.common.collect.Sets;

import de.faustedition.model.tei.TEIDocument;
import de.faustedition.util.XMLUtil;

public class CorpusDataBuilderTest
{
	private static final String[] TEST_RESOURCE_PATHS = new String[] { "/xstandoff/391098_0349.xml", "/xstandoff/391098_0360.xml", "/xstandoff/391098_0377.xml" };
	private CorpusDataBuildingHandler builder = new CorpusDataBuildingHandler();
	private static Transformer cleanupTransformer;

	@Test
	public void buildCorpusData() throws Exception
	{
		for (String testResourcePath : TEST_RESOURCE_PATHS)
		{
			cleanupTransformer.transform(new StreamSource(getClass().getResourceAsStream(testResourcePath)), new SAXResult(builder));
			CorpusData corpusData = builder.getCorpusData();
			Document document = XMLUtil.documentBuilder().newDocument();
			corpusData.serialize(document, new HashMap<String, String>());
			XMLUtil.serialize(document, System.out, false);
		}
	}

	@Test
	public void extractLayer() throws Exception
	{
		for (String testResourcePath : TEST_RESOURCE_PATHS)
		{
			cleanupTransformer.transform(new StreamSource(getClass().getResourceAsStream(testResourcePath)), new SAXResult(builder));
			CorpusData corpusData = builder.getCorpusData();
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

			Document document = XMLUtil.documentBuilder().newDocument();
			
			Map<String, String> namespaces = new HashMap<String, String>();
			namespaces.put(TEIDocument.TEI_NS_URI, "");
			corpusData.serialize(document, namespaces);
			XMLUtil.serialize(document, System.out, true);
		}
	}

	@Before
	public void setUp() throws Exception
	{
		cleanupTransformer = XMLUtil.newTransformer(new StreamSource(CorpusDataBuildingHandler.class.getResourceAsStream("/xsl/text-tei-cleanup.xsl")));
	}
}
