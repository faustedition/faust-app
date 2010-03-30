package de.faustedition.document.facsimile;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class FacsimileView extends JScrollPane implements PropertyChangeListener {

	private final FacsimileViewState state;

	public FacsimileView(FacsimileViewState state) {
		this.state = state;
		this.state.addPropertyChangeListener(this);

		setViewportView(new ImageView());
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if ("zoom".equals(evt.getPropertyName())) {
			getViewport().getView().setSize((int) (state.getWidth() * state.getZoom()),
					(int) (state.getHeight() * state.getZoom()));
		}
	}

	public class ImageView extends JPanel {

		public ImageView() {
			setBackground(Color.BLACK);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			Graphics2D g2 = (Graphics2D) g.create();
			Rectangle clipBounds = g.getClipBounds();
			if (clipBounds != null) {
				g2.setColor(Color.LIGHT_GRAY);
				g2.fill(clipBounds);
				g2.setColor(Color.BLACK);
				g2.drawString(String.format("%dx%d", (int) getSize().getWidth(), (int) getSize().getHeight()),
						(int) (clipBounds.getX() + 50), (int) (clipBounds.getY() + 50));
			} else {
				g2.drawString("Full view", 50, 50);
			}
		}
	}
}
