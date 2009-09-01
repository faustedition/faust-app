package de.faustedition.util;

import java.util.regex.Matcher;

public class PatternUtil {

	public static Integer getIntegerGroup(Matcher matcher, int group) {
		String groupValue = matcher.group(group);
		return (groupValue == null ? null : Integer.parseInt(groupValue));
	}

}
