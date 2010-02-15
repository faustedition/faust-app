package de.faustedition.web;

import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.view.xslt.XsltView;

import de.faustedition.ErrorUtil;

public class Tei2SvgView extends XsltView {
	public Tei2SvgView() {
		setContentType("image/svg+xml");
	}

	@Override
	protected Source getStylesheetSource() {
		try {
			ClassPathResource xslResource = new ClassPathResource("manuscript-tei-2-svg.xsl", Tei2SvgView.class);
			return new StreamSource(xslResource.getInputStream(), xslResource.getURI().toASCIIString());
		} catch (IOException e) {
			throw ErrorUtil.fatal(e, "I/O error while loading TEI-SVG conversion stylesheet");
		}
	}

}
