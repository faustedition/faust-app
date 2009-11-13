package net.xstandoff;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class AnnotationElement extends AnnotationNode
{
	public AnnotationElement(CorpusData corpusData, AnnotationNode parent, String namespace, String localName)
	{
		super(corpusData, parent, namespace, localName);
	}

	public AnnotationSegment getSegment()
	{
		return corpusData.getSegmentation().getSegment(this);
	}

	@Override
	public Node createNode(Node parent, Map<AnnotationSegment, String> segmentIds, Map<String, String> namespacePrefixes)
	{
		Document document = parent.getOwnerDocument();
		Element element = null;

		if (namespace == null || namespace.length() == 0)
		{
			element = document.createElement(localName);
		}
		else
		{
			String prefix = getOrCreatePrefix(namespacePrefixes);
			element = document.createElementNS(namespace, prefix + (prefix.length() == 0 ? "" : ":") + localName);
		}

		element.setAttributeNS(CorpusData.XSTANDOFF_NS_URI, "xsf:segment", segmentIds.get(getSegment()));
		return element;
	}

	@Override
	public AnnotationNode copy()
	{
		return new AnnotationElement(corpusData, null, namespace, localName);
	}
}
