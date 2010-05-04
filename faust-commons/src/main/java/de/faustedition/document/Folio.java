package de.faustedition.document;

/**
 * Blätter.
 * 
 * @author gregor
 * 
 */
public class Folio extends DocumentUnit {
	private Page recto;
	private Page verso;

	public Page getRecto() {
		return recto;
	}

	public void setRecto(Page recto) {
		this.recto = recto;
	}

	public Page getVerso() {
		return verso;
	}

	public void setVerso(Page verso) {
		this.verso = verso;
	}
}
