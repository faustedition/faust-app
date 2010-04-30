package de.faustedition.web;

import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.Assert;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.AbstractView;

import de.faustedition.tei.EncodedTextDocument;

public class Tei2PdfViewResolver implements ViewResolver, InitializingBean {
	private FopFactory fopFactory;
	private Templates tei2xslFoTemplates;

	public Tei2PdfViewResolver() {
	}

	public View resolveViewName(String viewName, Locale locale) throws Exception {
		return (WitnessController.WITNESS_VIEW_NAME.equals(viewName) ? tei2PdfView : null);
	}

	public void afterPropertiesSet() throws Exception {
		fopFactory = FopFactory.newInstance();
		tei2PdfView.setContentType("application/pdf");

		ClassPathResource xsl = new ClassPathResource("manuscript-tei-2-xsl-fo.xsl", Tei2PdfViewResolver.class);
		StreamSource xslSource = new StreamSource(xsl.getInputStream(), xsl.getURI().toASCIIString());
		tei2xslFoTemplates = TransformerFactory.newInstance().newTemplates(xslSource);
	}

	private AbstractView tei2PdfView = new AbstractView() {

		@Override
		protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
			EncodedTextDocument encodedTextDocument = (EncodedTextDocument) model.get("document");
			Assert.notNull(encodedTextDocument, "Model does not contain a TEI document to transform");

			response.setContentType(getContentType());
			ServletOutputStream outputStream = response.getOutputStream();

			FOUserAgent userAgent = new FOUserAgent(fopFactory);
			userAgent.setTitle("Digitale Faust-Edition");
			userAgent.setAuthor("Johann Wolfgang von Goethe");
			userAgent.setCreator("Digitale Faust-Edition");
			userAgent.setCreationDate(new Date());
			userAgent.setProducer("Digitale Faust-Edition");
			Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, userAgent, outputStream);

			Transformer transformer = tei2xslFoTemplates.newTransformer();
			transformer.transform(new DOMSource(encodedTextDocument.getDom()), new SAXResult(fop.getDefaultHandler()));
			outputStream.flush();
		}

	};
}
