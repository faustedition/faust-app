package de.faustedition.web.document;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.core.io.ClassPathResource;

import de.faustedition.model.tei.EncodedTextDocument;
import de.faustedition.util.ErrorUtil;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.DeepUnwrap;

public class Tei2XhtmlTransformer implements TemplateMethodModelEx {
	private static final ClassPathResource TEI_2_HTML_XSL_RESOURCE = new ClassPathResource("/xsl/tei-2-xhtml.xsl");
	private Templates tei2htmlTemplates;

	public Tei2XhtmlTransformer() {
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			StreamSource xslSource = new StreamSource(TEI_2_HTML_XSL_RESOURCE.getInputStream());
			tei2htmlTemplates = transformerFactory.newTemplates(xslSource);
		} catch (TransformerException e) {
			throw ErrorUtil.fatal(e, "XSL error while compiling TEI/XHTML conversion templates");
		} catch (IOException e) {
			throw ErrorUtil.fatal(e, "I/O error while compiling TEI/XHTML conversion templates");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object exec(List arguments) throws TemplateModelException {
		if (arguments.size() != 1) {
			throw new TemplateModelException("Please provide a transcription facet to transform");
		}

		Object argument = DeepUnwrap.unwrap((TemplateModel) arguments.get(0));
		if (!(argument instanceof EncodedTextDocument)) {
			throw new TemplateModelException("Please provide a transcription facet to transform");
		}

		try {
			StringWriter htmlTranscription = new StringWriter();
			StreamResult htmlResult = new StreamResult(htmlTranscription);

			EncodedTextDocument d = (EncodedTextDocument) argument;
			tei2htmlTemplates.newTransformer().transform(new DOMSource(d.getDom()), htmlResult);

			return htmlTranscription.toString();
		} catch (TransformerConfigurationException e) {
			throw new TemplateModelException("XSL error while transforming TEI to XHTML", e);
		} catch (TransformerException e) {
			throw new TemplateModelException("XSL error while transforming TEI to XHTML", e);
		}
	}

}
