package de.faustedition.document.facsimile;

import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class FacsimileViewControl extends JPanel implements ChangeListener, PropertyChangeListener {

	private final FacsimileViewState state;
	private JSpinner zoomSpinner;
	private JSpinner angleSpinner;

	public FacsimileViewControl(FacsimileViewState state) {
		this.state = state;
		this.state.addPropertyChangeListener(this);

		this.zoomSpinner = new JSpinner(new SpinnerNumberModel((double) state.getZoom(), 0.1, 1.0, 0.1));
		this.zoomSpinner.addChangeListener(this);
		this.angleSpinner = new JSpinner(new SpinnerNumberModel(state.getAngle(), -180, 180, 1));
		this.angleSpinner.addChangeListener(this);

		setLayout(new FlowLayout(FlowLayout.TRAILING, 5, 0));
		add(new JLabel("Zoom:"));
		add(zoomSpinner);
		add(new JLabel("Drehung:"));
		add(angleSpinner);
	}

	public void stateChanged(ChangeEvent e) {
		Object eventSource = e.getSource();
		if (zoomSpinner.equals(eventSource)) {
			state.setZoom(((Number) zoomSpinner.getValue()).floatValue());
		} else if (angleSpinner.equals(eventSource)) {
			state.setAngle(((Number) angleSpinner.getValue()).intValue());
		}
	}

	public void propertyChange(PropertyChangeEvent evt) {
		String propertyName = evt.getPropertyName();
		if ("zoom".equals(propertyName)) {
			zoomSpinner.setValue((double) state.getZoom());
		} else if ("angle".equals(propertyName)) {
			angleSpinner.setValue(state.getAngle());
		}
	}
}
