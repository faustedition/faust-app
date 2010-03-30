package de.faustedition.document;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

public class Zone extends DocumentObject {
	protected Shape shape;

	public Zone(Shape shape) {
		this.shape = shape;
	}

	public DocumentObject getParent() {
		return parent;
	}

	public Shape getShape() {
		return shape;
	}

	@Override
	public void add(DocumentObject child) {
		if (child instanceof Zone) {
			Zone childZone = (Zone) child;
			if (!shape.contains(childZone.shape.getBounds2D())) {
				throw new IllegalArgumentException(childZone.toString());
			}
		}

		super.add(child);
	}

	public Dimension getDimension() {
		Rectangle bounds = shape.getBounds();
		return new Dimension((int) Math.round(bounds.getWidth()), (int) Math.round(bounds.getHeight()));
	}

	@Override
	protected void doPaint(Graphics2D g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setColor(Color.GRAY);
		g2.draw(shape);
	}
}
