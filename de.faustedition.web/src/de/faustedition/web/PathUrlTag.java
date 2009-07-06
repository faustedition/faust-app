package de.faustedition.web;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class PathUrlTag extends SimpleTagSupport {
	private String path;

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public void doTag() throws JspException, IOException {
		getJspContext().getOut().write(path);
	}
}
