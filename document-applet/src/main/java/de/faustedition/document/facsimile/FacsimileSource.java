package de.faustedition.document.facsimile;

import java.awt.Image;
import java.io.IOException;

public interface FacsimileSource {
	int getWidth() throws IOException;

	int getHeight() throws IOException;

	Image get(int x, int y, int width, int height) throws IOException;
}
