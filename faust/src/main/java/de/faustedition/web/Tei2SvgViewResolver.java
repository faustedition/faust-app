package de.faustedition.web;

import java.io.PrintWriter;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.Assert;
import org.springframework.util.xml.TransformerUtils;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.AbstractView;

import de.faustedition.tei.EncodedTextDocument;

public class Tei2SvgViewResolver implements ViewResolver, InitializingBean {

	private Templates tei2SvgTemplates;

	public View resolveViewName(String viewName, Locale locale) throws Exception {
		return (WitnessController.WITNESS_VIEW_NAME.equals(viewName) ? tei2SvgView : null);
	}

	public void afterPropertiesSet() throws Exception {
		tei2SvgView.setContentType("image/svg+xml");

		ClassPathResource xsl = new ClassPathResource("manuscript-tei-2-svg.xsl", Tei2SvgViewResolver.class);
		StreamSource xslSource = new StreamSource(xsl.getInputStream(), xsl.getURI().toASCIIString());
		tei2SvgTemplates = TransformerFactory.newInstance().newTemplates(xslSource);
	}

	private AbstractView tei2SvgView = new AbstractView() {

		@Override
		protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
			EncodedTextDocument encodedTextDocument = (EncodedTextDocument) model.get("document");
			Assert.notNull(encodedTextDocument, "Model does not contain a TEI document to transform");
			
			response.setContentType(getContentType());
			response.setCharacterEncoding("UTF-8");

			PrintWriter out = response.getWriter();
			Transformer transformer = tei2SvgTemplates.newTransformer();
			TransformerUtils.enableIndenting(transformer);
			transformer.transform(new DOMSource(encodedTextDocument.getDom()), new StreamResult(out));
			out.flush();
		}

	};
}
