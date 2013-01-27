package de.faustedition.transcript.input;

public class TranscriptInvalidException extends RuntimeException {
	
	private static final long serialVersionUID = -4908081364109212728L;

	public TranscriptInvalidException(String msg) {
		super(msg);
	}
	
	public TranscriptInvalidException(Throwable e) {
		super(e);
	}
}
