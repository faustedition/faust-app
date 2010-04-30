package de.faustedition.document;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.JFrame;

import de.faustedition.document.facsimile.FacsimileView;
import de.faustedition.document.facsimile.FacsimileViewControl;
import de.faustedition.document.facsimile.FacsimileViewState;

public class FacsimileViewDriver {

	public static void main(String[] args) {
		try {
			JFrame frame = new JFrame("Document viewer test");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setLayout(new BorderLayout(5, 5));

			FacsimileViewState viewState = new FacsimileViewState(new DummyFacsimileSourceImpl());
			frame.getContentPane().add(new FacsimileViewControl(viewState), BorderLayout.NORTH);
			frame.getContentPane().add(new FacsimileView(viewState), BorderLayout.CENTER);
			
			frame.pack();
			frame.setSize(600, 400);
			frame.setVisible(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
