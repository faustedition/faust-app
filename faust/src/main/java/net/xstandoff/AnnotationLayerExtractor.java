package net.xstandoff;


public abstract class AnnotationLayerExtractor
{
	private AnnotationLayer from;
	private AnnotationLayer to;

	public AnnotationLayerExtractor(AnnotationLayer from, AnnotationLayer to)
	{
		super();
		if (!from.getCorpusData().equals(to.getCorpusData()))
		{
			throw new IllegalArgumentException();
		}

		this.from = from;
		this.to = to;
	}

	public void extract()
	{
		for (AnnotationElement element : from.getChildrenOfType(AnnotationElement.class))
		{
			extractNode(element, to);
		}
	}

	private void extractNode(AnnotationElement from, AnnotationNode toParent)
	{
		AnnotationNode to = toParent;
		if (extract(from))
		{
			AnnotationSegment segment = from.getParent().remove(from);
			toParent.add(to = transform(from));
			from.getCorpusData().getSegmentation().setSegment(to, segment);
		}

		for (AnnotationElement child : from.getChildrenOfType(AnnotationElement.class))
		{
			extractNode(child, to);
		}
	}

	public abstract boolean extract(AnnotationElement node);

	public abstract AnnotationElement transform(AnnotationElement element);

}
