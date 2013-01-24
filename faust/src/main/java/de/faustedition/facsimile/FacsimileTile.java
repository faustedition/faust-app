package de.faustedition.facsimile;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.google.common.base.Objects;
import com.google.common.collect.Iterators;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class FacsimileTile implements Serializable {
	public static final int SIZE = 256;

	private File file;
	private int zoom;
	private int x;
	private int y;

	public FacsimileTile() {
	}

	public FacsimileTile(File file) {
		this(file, 0, 0, 0);
	}

	public FacsimileTile(File file, int zoom, int x, int y) {
		this.file = file;
		this.zoom = zoom;
		this.x = x;
		this.y = y;
	}

	public ImageReader createImageReader() throws IOException {
		final ImageInputStream imageInputStream = ImageIO.createImageInputStream(file);
		final ImageReader imageReader = Iterators.get(ImageIO.getImageReaders(imageInputStream), 0);
		imageReader.setInput(imageInputStream);
		return imageReader;
	}

	public Dimension getDimension() throws IOException {
		ImageReader reader = null;
		try {
			reader = createImageReader();
			return new Dimension(reader.getWidth(0), reader.getHeight(0));
		} finally {
			if (reader != null) {
				reader.dispose();
			}
		}
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public int getZoom() {
		return zoom;
	}

	public void setZoom(int zoom) {
		this.zoom = zoom;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof FacsimileTile) {
			final FacsimileTile other = (FacsimileTile) obj;
			return (x == other.x) && (y == other.y) && (zoom == other.zoom) && file.equals(other.file);
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(x, y, zoom, file);
	}
}
