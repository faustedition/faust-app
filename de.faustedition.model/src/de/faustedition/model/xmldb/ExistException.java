package de.faustedition.model.xmldb;

public class ExistException extends Exception {

	private static final long serialVersionUID = 4346293471519580528L;

	public ExistException() {
		super();
	}

	public ExistException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExistException(String message) {
		super(message);
	}

	public ExistException(Throwable cause) {
		super(cause);
	}
}
