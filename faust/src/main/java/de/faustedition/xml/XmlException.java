package de.faustedition.xml;

public class XmlException extends RuntimeException {

	private static final long serialVersionUID = -8437108589166126855L;

	public XmlException() {
		super();
	}

	public XmlException(String message, Throwable cause) {
		super(message, cause);
	}

	public XmlException(String message) {
		super(message);
	}

	public XmlException(Throwable cause) {
		super(cause);
	}

}
