package de.faustedition.model.tei;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.xml.XMLConstants;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.stereotype.Service;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.google.common.collect.Lists;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.rng.CompactSchemaReader;

import de.faustedition.util.ErrorUtil;
import de.faustedition.util.XMLUtil;

@Service
public class EncodedTextDocumentValidator {
	public static final String SCHEMA_URI = "http://xml.faustedition.net/schema/faust-tei.rnc";
	private static final Pattern XMLNS_ATTR_URI_PATTERN = Pattern.compile(Pattern.quote(XMLConstants.XMLNS_ATTRIBUTE_NS_URI));
	private Schema schema;

	@PostConstruct
	public void init() throws Exception {
		schema = CompactSchemaReader.getInstance().createSchema(new InputSource(SCHEMA_URI),
				new PropertyMapBuilder().toPropertyMap());
	}

	public boolean isValid(EncodedTextDocument document) {
		return validate(document).isEmpty();
	}

	public List<String> validate(EncodedTextDocument document) {
		final List<String> errors = Lists.newLinkedList();
		PropertyMapBuilder propertyMapBuilder = new PropertyMapBuilder();
		propertyMapBuilder.put(ValidateProperty.ERROR_HANDLER, new ErrorHandler() {

			/**
			 * Filters namespace declaration related errors.
			 * 
			 * @param e
			 */
			private void register(SAXParseException e) {
				if (!XMLNS_ATTR_URI_PATTERN.matcher(e.getMessage()).find()) {
					errors.add(String.format("[%d:%d] %s", e.getLineNumber(), e.getColumnNumber(), e
							.getMessage()));
				}
			}

			@Override
			public void error(SAXParseException exception) throws SAXException {
				register(exception);
			}

			@Override
			public void fatalError(SAXParseException exception) throws SAXException {
				register(exception);
			}

			@Override
			public void warning(SAXParseException exception) throws SAXException {
				register(exception);
			}
		});

		Validator validator = schema.createValidator(propertyMapBuilder.toPropertyMap());
		try {
			byte[] documentData = XMLUtil.serialize(document.getDom(), true);
			XMLUtil.nullTransformer(true).transform(new StreamSource(new ByteArrayInputStream(documentData)),
					new SAXResult(validator.getContentHandler()));
			return errors;
		} catch (TransformerException e) {
			throw ErrorUtil.fatal(e, "XSLT error while validating TEI document");
		}
	}
}
