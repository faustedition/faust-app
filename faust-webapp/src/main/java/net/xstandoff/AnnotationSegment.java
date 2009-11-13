package net.xstandoff;

public class AnnotationSegment implements Comparable<AnnotationSegment>
{
	private int start;
	private int end;

	public AnnotationSegment(int start, int end)
	{
		this.start = start;
		this.end = end;
	}

	public int getStart()
	{
		return start;
	}

	public int getEnd()
	{
		return end;
	}

	@Override
	public int compareTo(AnnotationSegment o)
	{
		return (start == o.start ? o.end - end : start - o.start);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj != null && (obj instanceof AnnotationSegment))
		{
			AnnotationSegment other = (AnnotationSegment) obj;
			return (start == other.start) && (end == other.end);
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode()
	{
		return (17 * 59 + start) * 59 + end;
	}
}
