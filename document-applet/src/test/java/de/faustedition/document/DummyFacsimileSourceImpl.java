package de.faustedition.document;

import java.awt.Image;
import java.awt.Rectangle;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;

import de.faustedition.document.facsimile.FacsimileSource;

public class DummyFacsimileSourceImpl implements FacsimileSource {

	public Image get(int x, int y, int width, int height) throws IOException {
		ImageReader imageReader = getImageReader();
		ImageReadParam param = imageReader.getDefaultReadParam();
		param.setSourceRegion(new Rectangle(x, y, width, height));
		return imageReader.read(0, param);
	}

	public int getHeight() throws IOException {
		return getImageReader().getHeight(0);
	}

	public int getWidth() throws IOException {
		return getImageReader().getWidth(0);
	}

	private ImageReader getImageReader() throws IOException {
		ImageReader imageReader = ImageIO.getImageReadersByMIMEType("image/jpeg").next();
		imageReader.setInput(ImageIO.createImageInputStream(getClass().getResourceAsStream("/390883_0024.jpg")), true);
		return imageReader;
	}
}
