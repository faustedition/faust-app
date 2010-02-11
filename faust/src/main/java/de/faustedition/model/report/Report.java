package de.faustedition.model.report;

import java.io.PrintWriter;

public interface Report {
	String getSubject();

	void printBody(PrintWriter body);

	boolean isEmpty();
}
