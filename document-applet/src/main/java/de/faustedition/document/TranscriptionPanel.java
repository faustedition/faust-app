package de.faustedition.document;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class TranscriptionPanel extends JPanel {

	private final Zone zone;

	public TranscriptionPanel(Zone zone) {
		super(new BorderLayout());
		this.zone = zone;
		setPreferredSize(zone.getDimension());
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);		
		zone.paint((Graphics2D) g.create());
	}
}
