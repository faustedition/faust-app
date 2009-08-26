package de.faustedition.model.repository;

public class RepositoryObjectNotFoundException extends Exception {

	public RepositoryObjectNotFoundException() {
		super();
	}

	public RepositoryObjectNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public RepositoryObjectNotFoundException(String message) {
		super(message);
	}

	public RepositoryObjectNotFoundException(Throwable cause) {
		super(cause);
	}

}
