package de.faustedition.document;

import java.util.List;

/**
 * Bögen.
 * 
 * @author gregor
 * 
 */
public class Sheet extends DocumentUnit {
	private List<Folio> folios;

	public List<Folio> getFolios() {
		return folios;
	}

	public void setFolios(List<Folio> folios) {
		this.folios = folios;
	}
}
