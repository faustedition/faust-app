package de.faustedition.model.xstandoff;

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class CorpusDataBuildingHandler extends DefaultHandler
{
	private CorpusData corpusData;
	private StringBuilder primaryDataBuilder;
	private Stack<AnnotationNode> parents;
	private Stack<Integer> segmentStarts;

	public CorpusData getCorpusData()
	{
		return corpusData;
	}

	@Override
	public void startDocument() throws SAXException
	{
		primaryDataBuilder = new StringBuilder();
		parents = new Stack<AnnotationNode>();
		segmentStarts = new Stack<Integer>();

		corpusData = new CorpusData();
		AnnotationLevel level = new AnnotationLevel(corpusData, null);
		corpusData.getAnnotationLevels().add(level);

		AnnotationLayer layer = new AnnotationLayer(corpusData, level);
		level.getChildren().add(layer);

		parents.add(layer);
		segmentStarts.add(0);
	}

	@Override
	public void endDocument() throws SAXException
	{
		segmentStarts = null;
		parents = null;
		corpusData.setPrimaryData(primaryDataBuilder.toString());
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		parents.push(parents.peek().addElement(uri, localName, attributes));
		segmentStarts.push(primaryDataBuilder.length());
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		AnnotationElement element = (AnnotationElement) parents.pop();
		corpusData.getSegmentation().setSegment(element, new AnnotationSegment(segmentStarts.pop(), primaryDataBuilder.length()));
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		primaryDataBuilder.append(ch, start, length);
	}
}
