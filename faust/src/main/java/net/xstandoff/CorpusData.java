package net.xstandoff;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.XMLConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.faustedition.model.xml.XmlUtil;

public class CorpusData
{
	public static final String XSTANDOFF_NS_URI = "http://www.xstandoff.net/2009/xstandoff/1.1";
	public static final String XSTANDOFF_SCHEMA_URI = "http://www.xstandoff.net/2009/xstandoff/1.1/xsf.xsd";
	private static final String XSTANDOFF_VERSION = "1.0";
	private String id;
	private String primaryData;
	private List<AnnotationLevel> annotationLevels = new ArrayList<AnnotationLevel>();
	private CorpusDataSegmentation segmentation = new CorpusDataSegmentation();

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getPrimaryData()
	{
		return primaryData;
	}

	public void setPrimaryData(String primaryData)
	{
		this.primaryData = primaryData;
	}

	public List<AnnotationLevel> getAnnotationLevels()
	{
		return annotationLevels;
	}

	public CorpusDataSegmentation getSegmentation()
	{
		return segmentation;
	}

	public void serialize(Node parent, Map<String, String> namespacePrefixes)
	{
		namespacePrefixes.put(XMLConstants.XML_NS_URI, XMLConstants.XML_NS_PREFIX);
		
		Document document = XmlUtil.getDocument(parent);

		Element corpusDataElement = document.createElementNS(XSTANDOFF_NS_URI, "xsf:corpusData");
		corpusDataElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
		corpusDataElement.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "xsi:schemaLocation", XSTANDOFF_NS_URI + " " + XSTANDOFF_SCHEMA_URI);
		corpusDataElement.setAttributeNS(XSTANDOFF_NS_URI, "xsf:version", XSTANDOFF_VERSION);
		corpusDataElement.setAttributeNS(XMLConstants.XML_NS_URI, "xml:id", id == null ? UUID.randomUUID().toString() : id);
		parent.appendChild(corpusDataElement);

		Element primaryDataElement = document.createElementNS(XSTANDOFF_NS_URI, "xsf:primaryData");
		primaryDataElement.setAttributeNS(XSTANDOFF_NS_URI, "xsf:start", Long.toString(0));
		primaryDataElement.setAttributeNS(XSTANDOFF_NS_URI, "xsf:end", Long.toString(primaryData.length()));

		Element textualContentElement = document.createElementNS(XSTANDOFF_NS_URI, "xsf:textualContent");
		textualContentElement.setTextContent(primaryData);
		primaryDataElement.appendChild(textualContentElement);

		corpusDataElement.appendChild(primaryDataElement);

		Map<AnnotationSegment, String> segmentIds = segmentation.serialize(this, corpusDataElement);

		Element annotationElement = document.createElementNS(XSTANDOFF_NS_URI, "xsf:annotation");
		for (String namespaceUri : namespacePrefixes.keySet())
		{
			String prefix = namespacePrefixes.get(namespaceUri);
			annotationElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE + (prefix.length() == 0 ? "" : ":") + prefix, namespaceUri);
		}
		corpusDataElement.appendChild(annotationElement);

		for (AnnotationLevel level : annotationLevels)
		{
			level.serialize(annotationElement, segmentIds, namespacePrefixes);
		}
	}
}
