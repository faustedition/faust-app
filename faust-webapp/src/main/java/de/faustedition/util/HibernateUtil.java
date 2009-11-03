package de.faustedition.util;

import java.util.Iterator;

import org.hibernate.Criteria;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;

import com.google.common.collect.AbstractIterator;

public class HibernateUtil
{
	public static <T> Iterable<T> scroll(Criteria criteria, Class<T> type)
	{
		final ScrollableResults scrollableResults = criteria.scroll(ScrollMode.FORWARD_ONLY);
		return new Iterable<T>()
		{

			@Override
			public Iterator<T> iterator()
			{
				return new AbstractIterator<T>()
				{

					@SuppressWarnings("unchecked")
					@Override
					protected T computeNext()
					{
						return (T) (scrollableResults.next() ? scrollableResults.get()[0] : endOfData());
					}
				};
			}

		};
	}
}
