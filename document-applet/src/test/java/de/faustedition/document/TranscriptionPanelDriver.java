package de.faustedition.document;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;

import javax.swing.JFrame;

public class TranscriptionPanelDriver {

	public static void main(String[] args) {
		Zone child = new Zone(new Rectangle(200, 200, 50, 400));
		child.add(new Text("Hello World!"));
		
		Zone zone = new Zone(new Rectangle(0, 0, 800, 600));
		zone.add(new Zone(new Rectangle(10, 10, 50, 100)));
		zone.add(child);
		
		TranscriptionPanel panel = new TranscriptionPanel(zone);
		panel.setBackground(Color.WHITE);
		
		JFrame frame = new JFrame("Document viewer test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout(5, 5));
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}
}
