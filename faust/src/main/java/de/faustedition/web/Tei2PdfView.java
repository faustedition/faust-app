package de.faustedition.web;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.view.xslt.XsltView;

import de.faustedition.ErrorUtil;

public class Tei2PdfView extends XsltView {
	private FopFactory fopFactory;

	public Tei2PdfView() {
		fopFactory = FopFactory.newInstance();
		setContentType("application/pdf");
	}

	@Override
	protected Result createResult(HttpServletResponse response) throws Exception {
		FOUserAgent userAgent = new FOUserAgent(fopFactory);
		userAgent.setTitle("Digitale Faust-Edition");
		userAgent.setAuthor("Johann Wolfgang von Goethe");
		userAgent.setCreator("Digitale Faust-Edition");
		userAgent.setCreationDate(new Date());
		userAgent.setProducer("Digitale Faust-Edition");
		Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, userAgent, response.getOutputStream());

		return new SAXResult(fop.getDefaultHandler());
	}

	@Override
	protected Source getStylesheetSource() {
		try {
			ClassPathResource xslResource = new ClassPathResource("manuscript-tei-2-xsl-fo.xsl", Tei2PdfView.class);
			return new StreamSource(xslResource.getInputStream(), xslResource.getURI().toASCIIString());
		} catch (IOException e) {
			throw ErrorUtil.fatal(e, "I/O error while loading TEI-PDF conversion stylesheet");
		}
	}
}
