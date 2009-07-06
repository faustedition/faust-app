package de.faustedition.model.service;

import java.util.List;
import java.util.regex.Pattern;

import de.faustedition.model.metadata.MetadataValue;

public class MetadataQuery {
	public static final int PAGE_SIZE = 25;
	
	private String query;
	private int page = 1;
	private List<MetadataValue> result;
	private int totalResults;

	public MetadataQuery() {
	}

	public MetadataQuery(String query, int page) {
		this.query = query;
		this.page = page;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getSQLQuery() {
		if (this.query == null) {
			return null;
		}

		String sqlQuery = this.query;
		sqlQuery = sqlQuery.replaceAll(Pattern.quote("%"), "\\%");
		sqlQuery = sqlQuery.replaceAll(Pattern.quote("_"), "\\_");

		if (sqlQuery.contains("*") || sqlQuery.contains("?")) {
			sqlQuery = sqlQuery.replaceAll(Pattern.quote("*"), "%");
			sqlQuery = sqlQuery.replaceAll(Pattern.quote("?"), "_");
			return sqlQuery;
		}

		return "%" + sqlQuery + "%";
	}

	public List<MetadataValue> getResult() {
		return result;
	}

	public void setResult(List<MetadataValue> result) {
		this.result = result;
	}

	public int getTotalResults() {
		return totalResults;
	}

	public void setTotalResults(int totalResults) {
		this.totalResults = totalResults;
	}
}
