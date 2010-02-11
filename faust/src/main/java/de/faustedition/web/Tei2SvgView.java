package de.faustedition.web;

import org.springframework.web.servlet.view.xslt.XsltView;

public class Tei2SvgView extends XsltView {
	public Tei2SvgView() {
		setContentType("image/svg+xml");
		setUrl("classpath:/xsl/manuscript-tei-2-svg.xsl");
	}
}
