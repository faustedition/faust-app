package de.faustedition.model.xstandoff;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;
import org.xml.sax.Attributes;

public abstract class AnnotationNode
{
	protected CorpusData corpusData;
	protected AnnotationNode parent;
	protected String namespace;
	protected String localName;
	private List<AnnotationNode> children = new ArrayList<AnnotationNode>();

	public AnnotationNode(CorpusData corpusData, AnnotationNode parent, String namespace, String localName)
	{
		super();
		this.corpusData = corpusData;
		this.parent = parent;
		this.namespace = namespace;
		this.localName = localName;
	}

	public CorpusData getCorpusData()
	{
		return corpusData;
	}

	public AnnotationNode getParent()
	{
		return parent;
	}

	public void setParent(AnnotationNode parent)
	{
		this.parent = parent;
	}

	public String getNamespace()
	{
		return namespace;
	}

	public String getLocalName()
	{
		return localName;
	}

	public List<AnnotationNode> getChildren()
	{
		return children;
	}

	@SuppressWarnings("unchecked")
	public <T extends AnnotationNode> List<T> getChildrenOfType(Class<T> type)
	{
		ArrayList<T> childrenOfType = new ArrayList<T>(children.size());
		for (AnnotationNode child : children)
		{
			if (type.isAssignableFrom(child.getClass()))
			{
				childrenOfType.add((T) child);
			}
		}
		return childrenOfType;
	}

	public AnnotationElement addElement(String namespaceUri, String localName, Attributes attributes)
	{
		checkForValidParentElement();
		AnnotationElement annotationElement = new AnnotationElement(corpusData, this, namespaceUri, localName);
		for (int ac = 0; ac < attributes.getLength(); ac++)
		{
			annotationElement.addAttribute(attributes.getURI(ac), attributes.getLocalName(ac), attributes.getValue(ac));
		}

		add(annotationElement);
		return annotationElement;
	}

	public AnnotationAttribute addAttribute(String namespaceUri, String localName, String value)
	{
		checkForValidParentElement();
		AnnotationAttribute annotationAttribute = new AnnotationAttribute(corpusData, this, namespaceUri, localName, value);
		add(annotationAttribute);
		return annotationAttribute;
	}

	public void add(AnnotationNode node)
	{
		node.setParent(this);
		children.add(node);
	}

	public AnnotationSegment remove(AnnotationNode node)
	{
		boolean removed = children.remove(node);
		if (!removed)
		{
			throw new IllegalArgumentException();
		}
		for (AnnotationNode child : node.getChildren())
		{
			add(child);
		}
		return corpusData.getSegmentation().remove(node);
	}

	public void serialize(Node parent, Map<AnnotationSegment, String> segmentIds, Map<String, String> namespacePrefixes)
	{
		Node node = createNode(parent, segmentIds, namespacePrefixes);
		parent.appendChild(node);

		for (AnnotationNode child : children)
		{
			child.serialize(node, segmentIds, namespacePrefixes);
		}
	}

	protected String getOrCreatePrefix(Map<String, String> namespacePrefixes)
	{
		String prefix = namespacePrefixes.get(namespace);
		if (prefix == null)
		{
			prefix = "ns" + namespacePrefixes.size();
			namespacePrefixes.put(namespace, prefix);
		}
		return prefix;
	}

	public abstract Node createNode(Node parent, Map<AnnotationSegment, String> segmentIds, Map<String, String> namespacePrefixes);

	private void checkForValidParentElement()
	{
		if (!(this instanceof AnnotationElement) && !(this instanceof AnnotationLayer))
		{
			throw new IllegalArgumentException();
		}
	}

	public abstract AnnotationNode copy();
}
