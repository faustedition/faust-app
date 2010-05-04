package de.faustedition.document;

/**
 * Maße.
 * 
 * <p>
 * Maße werden ganzzahlig in Milimeter angegeben.
 * </p>
 * 
 * @author gregor
 * 
 */
public class Extent {
	/**
	 * Horizontale Ausdehnung.
	 */
	private Integer horizontal;

	/**
	 * Vertikale Ausdehnung.
	 */
	private Integer vertical;

	/**
	 * Tiefe/ Stärke.
	 */
	private Integer depth;

	public Integer getHorizontal() {
		return horizontal;
	}

	public void setHorizontal(Integer horizontal) {
		this.horizontal = horizontal;
	}

	public Integer getVertical() {
		return vertical;
	}

	public void setVertical(Integer vertical) {
		this.vertical = vertical;
	}

	public Integer getDepth() {
		return depth;
	}

	public void setDepth(Integer depth) {
		this.depth = depth;
	}
}
