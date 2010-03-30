package de.faustedition.document.facsimile;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;

public class FacsimileViewState {

	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	private FacsimileSource source;
	private int width;
	private int height;
	private Float zoom = 0.1f;
	private int angle = 0;
	private int x = 0;
	private int y = 0;

	public FacsimileViewState(FacsimileSource source) throws IOException {
		this.source = source;
		this.width = source.getWidth();
		this.height = source.getHeight();
	}

	public FacsimileSource getSource() {
		return source;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public Float getZoom() {
		return zoom;
	}

	public void setZoom(Float zoom) {
		if (this.zoom != zoom && zoom > 0) {
			Float oldValue = this.zoom;
			this.zoom = zoom;
			propertyChangeSupport.firePropertyChange("zoom", oldValue, zoom);
		}
	}

	public int getAngle() {
		return angle;
	}

	public void setAngle(int angle) {
		if (this.angle != angle) {
			int oldValue = this.angle;
			this.angle = angle;
			propertyChangeSupport.firePropertyChange("angle", oldValue, angle);
		}
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
		if (this.x != x) {
			int oldValue = this.x;
			this.x = x;
			propertyChangeSupport.firePropertyChange("x", oldValue, x);
		}
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
		if (this.y != y) {
			int oldValue = this.y;
			this.y = y;
			propertyChangeSupport.firePropertyChange("y", oldValue, y);
		}
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
}
