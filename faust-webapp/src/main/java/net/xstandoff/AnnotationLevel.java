package net.xstandoff;

import java.util.Map;

import javax.xml.XMLConstants;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class AnnotationLevel extends AnnotationNode
{

	private String id;

	public AnnotationLevel(CorpusData corpusData, AnnotationNode parent)
	{
		super(corpusData, parent, CorpusData.XSTANDOFF_NS_URI, "level");
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj != null && id != null && obj instanceof AnnotationLevel)
		{
			return id.equals(((AnnotationLevel) obj).id);
		}

		return super.equals(obj);
	}

	@Override
	public int hashCode()
	{
		return (id == null ? super.hashCode() : id.hashCode());
	}

	@Override
	public Node createNode(Node parent, Map<AnnotationSegment, String> segmentIds, Map<String, String> namespacePrefixes)
	{
		Element levelElement = parent.getOwnerDocument().createElementNS(CorpusData.XSTANDOFF_NS_URI, "xsf:level");
		if (id != null)
		{
			levelElement.setAttributeNS(XMLConstants.XML_NS_URI, "xml:id", id);
		}
		return levelElement;
	}

	@Override
	public AnnotationNode copy()
	{
		return new AnnotationLevel(corpusData, null);
	}
}
