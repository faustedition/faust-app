package de.faustedition.model.xstandoff;

import java.util.Map;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class AnnotationAttribute extends AnnotationNode
{
	private String value;

	public AnnotationAttribute(CorpusData corpusData, AnnotationNode parent, String namespace, String localName, String value)
	{
		super(corpusData, parent, namespace, localName);
		this.value = value;
	}

	public String getValue()
	{
		return value;
	}

	@Override
	public void serialize(Node parent, Map<AnnotationSegment, String> segmentIds, Map<String, String> namespacePrefixes)
	{
		Document document = parent.getOwnerDocument();
		Attr attr = null;

		if (namespace == null || namespace.length() == 0)
		{
			attr = document.createAttribute(localName);
		}
		else
		{
			String prefix = getOrCreatePrefix(namespacePrefixes);
			attr = document.createAttributeNS(namespace, prefix + (prefix.length() == 0 ? "" : ":") + localName);
		}

		attr.setValue(value);
		((Element) parent).setAttributeNode(attr);
	}

	@Override
	public Node createNode(Node parent, Map<AnnotationSegment, String> segmentIds, Map<String, String> namespacePrefixes)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public AnnotationNode copy()
	{
		return new AnnotationAttribute(corpusData, null, namespace, localName, value);
	}
}