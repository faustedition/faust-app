package de.faustedition.web.search;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigation;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.compass.core.Compass;
import org.compass.core.CompassHit;
import org.compass.core.support.search.CompassSearchCommand;
import org.compass.core.support.search.CompassSearchHelper;
import org.compass.core.support.search.CompassSearchResults;

import de.faustedition.model.manuscript.Transcription;
import de.faustedition.model.metadata.MetadataAssignment;
import de.faustedition.web.PageBase;

public class SearchPage extends PageBase
{
	private static final int RESULTS_PAGE_SIZE = 20;

	@SpringBean
	private Compass compass;

	private String query;

	private int pageNumber;

	private static PageParameters getPageParameters(String query, int pageNumber)
	{
		PageParameters parameters = new PageParameters();
		if (StringUtils.isNotBlank(query))
		{
			parameters.add("0", query);
			if (pageNumber > 0)
			{
				parameters.add("1", Integer.toString(pageNumber));
			}
		}
		return parameters;
	}

	private static BookmarkablePageLink<SearchPage> getLink(String id, String query, int pageNumber)
	{
		return new BookmarkablePageLink<SearchPage>(id, SearchPage.class, getPageParameters(query, pageNumber));
	}

	public SearchPage(PageParameters parameters)
	{
		this.query = parameters.getString("0");
		this.pageNumber = parameters.getAsInteger("1", 0);

		add(new SearchForm("form"));

		SearchResultsDataView resultsDataView = new SearchResultsDataView("results");
		resultsDataView.setItemsPerPage(RESULTS_PAGE_SIZE);
		resultsDataView.setCurrentPage(pageNumber);
		add(resultsDataView);

		add(new SearchResultsPagingNavigator("resultsPager", resultsDataView));

	}

	public String getQuery()
	{
		return query;
	}

	public void setQuery(String query)
	{
		this.query = query;
	}

	public int getPageNumber()
	{
		return pageNumber;
	}

	public void setPageNumber(int pageNumber)
	{
		this.pageNumber = pageNumber;
	}

	@Override
	public String getPageTitle()
	{
		return "Suche";
	}

	private class SearchResultsDataView extends DataView<CompassHit>
	{

		protected SearchResultsDataView(String id)
		{
			super(id, new SearchResultsDataProvider());
		}

		@Override
		protected void populateItem(Item<CompassHit> item)
		{
			CompassHit hit = item.getModelObject();
			Object hitObject = hit.data();
			if (hitObject instanceof MetadataAssignment)
			{
				MetadataAssignment assignment = (MetadataAssignment) hitObject;
				item.add(new Label("hit", assignment.getField() + " ==> " + assignment.getValue()));
			} else if (hitObject instanceof Transcription)
			{
				Transcription transcription = (Transcription) hitObject;
				item.add(new Label("hit", "Transcription #" + transcription.getId()));
			} else
			{
				item.add(new Label("hit", "\u00a0"));
			}
			// item.add(new Label("resultLabel",
			// String.format("%d. %s (%d %%)", item.getIndex() + 1,
			// hit.data().getClass(), Math.round(hit.getScore() *
			// 100))));
		}

		@Override
		public boolean isVisible()
		{
			return StringUtils.isNotBlank(getQuery());
		}
	}

	private class SearchResultsDataProvider implements IDataProvider<CompassHit>
	{

		public SearchResultsDataProvider()
		{
		}

		@Override
		public Iterator<? extends CompassHit> iterator(int first, int count)
		{
			CompassSearchResults searchResults = new CompassSearchHelper(compass, count).search(new CompassSearchCommand(query, first / RESULTS_PAGE_SIZE));
			return Arrays.asList(searchResults.getHits()).iterator();
		}

		@Override
		public IModel<CompassHit> model(CompassHit object)
		{
			return Model.of(object);
		}

		@Override
		public int size()
		{
			return new CompassSearchHelper(compass).search(new CompassSearchCommand(query)).getTotalHits();
		}

		@Override
		public void detach()
		{
		}

	}

	private class SearchForm extends StatelessForm<Void>
	{
		public SearchForm(String id)
		{
			super(id);
			add(new TextField<String>("query", new PropertyModel<String>(SearchPage.this, "query")));
		}

		@Override
		protected void onSubmit()
		{
			setResponsePage(SearchPage.class, getPageParameters(query, 0));
		}
	}

	private class SearchResultsPagingNavigator extends PagingNavigator
	{

		public SearchResultsPagingNavigator(String id, SearchResultsDataView resultsView)
		{
			super(id, resultsView);
		}

		@Override
		protected AbstractLink newPagingNavigationIncrementLink(String id, IPageable pageable, int increment)
		{
			return newPagingNavigationLink(id, pageable, pageNumber + increment);
		}

		@Override
		protected AbstractLink newPagingNavigationLink(String id, IPageable pageable, int pageNumber)
		{
			return getLink(id, query, Math.max(0, Math.min(pageable.getPageCount() - 1, pageNumber)));
		}

		@Override
		protected PagingNavigation newNavigation(IPageable pageable, IPagingLabelProvider labelProvider)
		{
			return new PagingNavigation("navigation", pageable, labelProvider)
			{
				@Override
				protected AbstractLink newPagingNavigationLink(String id, IPageable pageable, int pageIndex)
				{
					return getLink(id, query, Math.max(0, Math.min(pageable.getPageCount() - 1, pageIndex)));
				}
			};
		}

		@Override
		public boolean isVisible()
		{
			return StringUtils.isNotBlank(getQuery()) && (getPageable().getPageCount() > 1);
		}
	}
}
