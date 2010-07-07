package de.faustedition.facsimile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FacsimileProperties {
	private static final Pattern SPEC_PATTERN = Pattern.compile("\\A(\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\S+)");
	private int width;
	private int height;
	private int horizontalResolution;
	private int verticalResolution;
	private int colorDepth;
	private String colorModel;

	private FacsimileProperties() {
	}

	public static FacsimileProperties fromSpec(String spec) {
		Matcher specMatcher = SPEC_PATTERN.matcher(spec);
		if (!specMatcher.find()) {
			throw new IllegalArgumentException(spec);
		}

		FacsimileProperties props = new FacsimileProperties();
		props.width = Integer.parseInt(specMatcher.group(1));
		props.height = Integer.parseInt(specMatcher.group(2));
		props.horizontalResolution = Integer.parseInt(specMatcher.group(3));
		props.verticalResolution = Integer.parseInt(specMatcher.group(4));
		props.colorDepth = Integer.parseInt(specMatcher.group(5));
		props.colorModel = specMatcher.group(6);

		return props;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getHorizontalResolution() {
		return horizontalResolution;
	}

	public int getVerticalResolution() {
		return verticalResolution;
	}

	public int getColorDepth() {
		return colorDepth;
	}

	public String getColorModel() {
		return colorModel;
	}
}
