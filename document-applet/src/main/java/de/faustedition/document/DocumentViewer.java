package de.faustedition.document;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.text.MessageFormat;

import javax.swing.AbstractAction;
import javax.swing.JApplet;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.OverlayLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.AbstractPanInteractor;
import org.apache.batik.util.XMLResourceDescriptor;

public class DocumentViewer extends JApplet {
	@SuppressWarnings("unchecked")
	@Override
	public void init() {
		super.init();
		setLayout(new BorderLayout(5, 5));
		try {

			URL svgData = null;
			if (getParameter("svgSrc") != null) {
				svgData = new URL(getDocumentBase(), getParameter("svgSrc"));

			} else if (getParameter("svgUri") != null) {
				svgData = new URL(getParameter("svgUri"));
			} else {
				throw new IllegalArgumentException("No applet parameter given for SVG source");
			}
			final JSVGCanvas transcriptionCanvas = new JSVGCanvas();
			transcriptionCanvas.setDocument(new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName())
					.createSVGDocument(svgData.toExternalForm()));
			transcriptionCanvas.setBackground(new Color(1f, 1f, 1f, 0f));
			transcriptionCanvas.getInteractors().clear();
			transcriptionCanvas.getInteractors().add(new AbstractPanInteractor() {
				@Override
				public boolean startInteraction(InputEvent ie) {
					return ie.getID() == MouseEvent.MOUSE_PRESSED;
				}
			});
			final JPanel facsimilePanel = new JPanel() {
				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					Graphics2D g2 = (Graphics2D) g;

					BufferedImage backgroundImage = g2.getDeviceConfiguration().createCompatibleImage(200, 60,
							Transparency.TRANSLUCENT);
					Graphics2D backgroundGraphics = (Graphics2D) backgroundImage.getGraphics();

					backgroundGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON);
					backgroundGraphics.setFont(new Font("Serif", Font.ITALIC, 30));
					backgroundGraphics.setColor(Color.LIGHT_GRAY);
					backgroundGraphics.drawString("Facsimile", 10, 40);

					g2.setPaint(new TexturePaint(backgroundImage, new Rectangle(0, 0, 200, 60)));
					g2.fillRect(0, 0, getWidth(), getHeight());
				}
			};

			final JSlider zoomSlider = new JSlider(10, 30, 10);
			zoomSlider.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {

					AffineTransform at = transcriptionCanvas.getRenderingTransform();

					AffineTransform scaleAt = new AffineTransform();
					double scale = (zoomSlider.getValue() / 10.0d) / at.getScaleX();
					scaleAt.scale(scale, scale);

					scaleAt.concatenate(at);
					transcriptionCanvas.setRenderingTransform(scaleAt);
					transcriptionCanvas.setSize((int) (transcriptionCanvas.getSize().width * scale),
							(int) (transcriptionCanvas.getSize().height * scale));
				}
			});
			JCheckBox facsimileCheckBox = new JCheckBox(new AbstractAction("Facsimile") {

				public void actionPerformed(ActionEvent e) {
					facsimilePanel.setVisible(((JCheckBox) e.getSource()).isSelected());
				}
			});
			facsimileCheckBox.setSelected(true);
			JCheckBox transcriptionCheckBox = new JCheckBox(new AbstractAction("Transkription") {

				public void actionPerformed(ActionEvent e) {
					transcriptionCanvas.setVisible(((JCheckBox) e.getSource()).isSelected());
				}
			});
			transcriptionCheckBox.setSelected(true);

			JToolBar toolbar = new JToolBar();
			toolbar.setFloatable(false);
			toolbar.add(facsimileCheckBox);
			toolbar.add(transcriptionCheckBox);
			toolbar.add(zoomSlider);
			add(toolbar, BorderLayout.NORTH);

			JPanel overlayPanel = new JPanel();
			overlayPanel.setLayout(new OverlayLayout(overlayPanel));
			overlayPanel.add(transcriptionCanvas);
			overlayPanel.add(facsimilePanel);
			add(overlayPanel, BorderLayout.CENTER);
		} catch (Exception e) {
			String message = MessageFormat.format("Cannot load SVG source: {0}", e.getMessage());
			JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException(message, e);
		}
	}
}
