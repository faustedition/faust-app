package de.faustedition.model.xstandoff;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.XMLConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class CorpusDataSegmentation
{
	private static final String SEGMENT_XML_ID_PREFIX = "s";

	private Map<AnnotationNode, AnnotationSegment> segmentIndex = new HashMap<AnnotationNode, AnnotationSegment>();
	private SortedMap<AnnotationSegment, Set<AnnotationNode>> nodeRegister = new TreeMap<AnnotationSegment, Set<AnnotationNode>>();

	public void setSegment(AnnotationNode node, AnnotationSegment segment)
	{
		remove(node);

		if (nodeRegister.containsKey(segment))
		{
			nodeRegister.get(segment).add(node);
		}
		else
		{
			HashSet<AnnotationNode> nodeList = new HashSet<AnnotationNode>();
			nodeList.add(node);
			nodeRegister.put(segment, nodeList);
		}

		segmentIndex.put(node, segment);
	}

	public AnnotationSegment remove(AnnotationNode node)
	{
		if (segmentIndex.containsKey(node))
		{
			AnnotationSegment segment = segmentIndex.get(node);
			segmentIndex.remove(node);
			nodeRegister.get(segment).remove(node);
			return segment;
		}

		return null;
	}

	public AnnotationSegment getSegment(AnnotationNode node)
	{
		return segmentIndex.get(node);
	}

	public Map<AnnotationSegment, String> serialize(CorpusData corpusData, Node parent)
	{
		Map<AnnotationSegment, String> segmentIds = new HashMap<AnnotationSegment, String>();
		Document document = parent.getOwnerDocument();

		Element segmentationElement = document.createElementNS(CorpusData.XSTANDOFF_NS_URI, "xsf:segmentation");

		int segmentId = 0;
		for (AnnotationSegment segment : nodeRegister.keySet())
		{
			Element segmentElement = document.createElementNS(CorpusData.XSTANDOFF_NS_URI, "xsf:segment");
			segmentElement.setAttributeNS(CorpusData.XSTANDOFF_NS_URI, "xsf:type", "char");
			segmentElement.setAttributeNS(CorpusData.XSTANDOFF_NS_URI, "xsf:start", Integer.toString(segment.getStart()));
			segmentElement.setAttributeNS(CorpusData.XSTANDOFF_NS_URI, "xsf:end", Integer.toString(segment.getEnd()));
			segmentElement.setAttributeNS(XMLConstants.XML_NS_URI, "xml:id", SEGMENT_XML_ID_PREFIX + (++segmentId));
			segmentIds.put(segment, SEGMENT_XML_ID_PREFIX + segmentId);

			segmentationElement.appendChild(segmentElement);
		}

		parent.appendChild(segmentationElement);
		return segmentIds;
	}
}
