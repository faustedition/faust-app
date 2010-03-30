package de.faustedition.document;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

public class Text extends DocumentObject {

	private final String text;

	public Text(String text) {
		this.text = text;
	}

	@Override
	protected void doPaint(Graphics2D g) {
		Shape zoneShape = getAncestorOfType(Zone.class).getShape();
		Rectangle zoneBounds = zoneShape.getBounds();

		Graphics2D g2 = (Graphics2D) g.create();
		g2.clip(zoneShape);
		g2.translate(zoneBounds.getX(), zoneBounds.getY());
		
		FontMetrics fm = g2.getFontMetrics();		
		g2.drawString(text, 0, fm.getHeight());
		
	}
}
