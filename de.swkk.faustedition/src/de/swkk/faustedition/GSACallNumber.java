package de.swkk.faustedition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class GSACallNumber implements Comparable<GSACallNumber> {
	private static final Pattern CALL_NUMBER_PATTERN = Pattern.compile("^(GSA\\s*)?(([0-9]+)/)?([XVI]+),([0-9]+)(,([0-9a-z:]+))?([,\\*]([0-9a-z]+?))?$");

	private String value;
	private Integer portfolio;
	private String subPortfolio;
	private Integer file;
	private String subFile;
	private String content;

	public GSACallNumber(String callNumber) {
		Matcher callNumberMatcher = CALL_NUMBER_PATTERN.matcher(callNumber);
		if (!callNumberMatcher.matches()) {
			throw new IllegalArgumentException(callNumber);
		}

		this.value = callNumber;
		this.portfolio = getIntegerGroup(callNumberMatcher, 3);
		this.subPortfolio = callNumberMatcher.group(4);
		this.file = getIntegerGroup(callNumberMatcher, 5);
		this.subFile = callNumberMatcher.group(7);
		this.content = callNumberMatcher.group(9);
	}

	public String getValue() {
		return value;
	}

	public Integer getPortfolio() {
		return portfolio;
	}

	public String getSubPortfolio() {
		return subPortfolio;
	}

	public Integer getFile() {
		return file;
	}

	public String getSubFile() {
		return subFile;
	}

	public String getContent() {
		return content;
	}

	public boolean isContent() {
		return StringUtils.isNotBlank(content);
	}

	public String getFileValue() {
		return isContent() ? StringUtils.stripEnd(StringUtils.removeEnd(value, content), ",*") : value;
	}
	
	@Override
	public boolean equals(Object obj) {
		if ((obj != null) && getClass().equals(obj.getClass())) {
			return this.value.equalsIgnoreCase(((GSACallNumber) obj).value);
		}

		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return this.value.toUpperCase().hashCode();
	}

	@Override
	public String toString() {
		return this.value;
	}

	private static Integer getIntegerGroup(Matcher matcher, int group) {
		String groupValue = matcher.group(group);
		return (groupValue == null ? null : Integer.parseInt(groupValue));
	}

	public int compareTo(GSACallNumber o) {
		return toString().compareToIgnoreCase(o.toString());
	}
}
