package de.swkk.metadata;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import de.faustedition.PatternUtil;

public class GSACallNumber implements Comparable<GSACallNumber> {
	private static final Pattern CALL_NUMBER_PATTERN = Pattern.compile("^(GSA\\s*)?(([0-9]+)/)?([XVI]+),([0-9]+)(,([0-9]+)(([a-z])?([,:0-9a-z]+)?)?)?(\\*([0-9a-z]+?))?$");

	private Integer portfolio;
	private String subPortfolio;
	private Integer file;
	private Integer subFile;
	private String subFilePrimarySuffix;
	private String subFileSecondarySuffix;
	private String contentSpec;

	public GSACallNumber(String callNumber) {
		Matcher matcher = CALL_NUMBER_PATTERN.matcher(callNumber);
		if (!matcher.matches()) {
			throw new IllegalArgumentException(callNumber);
		}

		this.portfolio = PatternUtil.getIntegerGroup(matcher, 3);
		this.subPortfolio = StringUtils.trimToNull(matcher.group(4));
		this.file = PatternUtil.getIntegerGroup(matcher, 5);
		this.subFile = PatternUtil.getIntegerGroup(matcher, 7);
		this.subFilePrimarySuffix = StringUtils.trimToNull(matcher.group(9));
		this.subFileSecondarySuffix = StringUtils.trimToNull(matcher.group(10));
		this.contentSpec = StringUtils.trimToNull(matcher.group(12));
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

	public Integer getSubFile() {
		return subFile;
	}

	public String getSubFilePrimarySuffix() {
		return subFilePrimarySuffix;
	}

	public String getSubFileSecondarySuffix() {
		return subFileSecondarySuffix;
	}

	public String getContentSpec() {
		return contentSpec;
	}

	public boolean hasContentPart() {
		return StringUtils.isNotBlank(contentSpec);
	}

	@Override
	public boolean equals(Object obj) {
		if ((obj != null) && getClass().equals(obj.getClass())) {
			GSACallNumber other = (GSACallNumber) obj;
			return new EqualsBuilder().append(portfolio, other.portfolio).append(subPortfolio, other.subPortfolio).append(file, other.file).append(subFile, other.subFile).append(
					subFilePrimarySuffix, other.subFilePrimarySuffix).append(subFileSecondarySuffix, other.subFileSecondarySuffix).append(contentSpec, other.contentSpec)
					.isEquals();
		}

		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(portfolio).append(subPortfolio).append(file).append(subFile).append(subFilePrimarySuffix).append(subFileSecondarySuffix).append(contentSpec)
				.toHashCode();
	}

	@Override
	public String toString() {
		StringBuilder callNumberStr = new StringBuilder();
		callNumberStr.append(portfolio == null ? "" : portfolio);
		callNumberStr.append(subPortfolio == null ? "" : (callNumberStr.length() == 0 ? "" : "/") + subPortfolio);
		callNumberStr.append(file == null ? "" : (callNumberStr.length() == 0 ? "" : ",") + file);
		callNumberStr.append(subFile == null ? "" : (callNumberStr.length() == 0 ? "" : ",") + subFile);
		callNumberStr.append(subFilePrimarySuffix == null ? "" : subFilePrimarySuffix);
		callNumberStr.append(subFileSecondarySuffix == null ? "" : subFileSecondarySuffix);
		callNumberStr.append(contentSpec == null ? "" : (callNumberStr.length() == 0 ? "" : "*") + contentSpec);
		return callNumberStr.toString();
	}

	public String dump() {
		return ToStringBuilder.reflectionToString(this);
	}

	public int compareTo(GSACallNumber o) {
		return new CompareToBuilder().append(portfolio, o.portfolio).append(subPortfolio, o.subPortfolio).append(file, o.file).append(subFile, o.subFile).append(subFilePrimarySuffix,
				o.subFilePrimarySuffix).append(subFileSecondarySuffix, o.subFileSecondarySuffix).append(contentSpec, o.contentSpec).toComparison();
	}

	public boolean contains(GSACallNumber callNumber) {
		String[] thisComponents = toStringComponents();
		String[] otherComponents = callNumber.toStringComponents();
		for (int cc = 0; cc < thisComponents.length; cc++) {
			String thisComp = thisComponents[cc];
			String otherComp = otherComponents[cc];
			if (thisComp != null) {
				if (otherComp == null || !(thisComp.equals(otherComp))) {
					return false;
				}
			}
		}

		return true;
	}

	private String[] toStringComponents() {
		return new String[] { (portfolio == null ? null : portfolio.toString()), subPortfolio, (file == null ? null : file.toString()), (subFile == null ? null : subFile.toString()),
				subFilePrimarySuffix, subFileSecondarySuffix, contentSpec };
	}
}
