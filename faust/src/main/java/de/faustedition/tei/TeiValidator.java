package de.faustedition.tei;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.rng.SAXSchemaReader;
import com.thaiopensource.xml.sax.Sax2XMLReaderCreator;

import de.faustedition.FaustURI;
import de.faustedition.xml.XMLStorage;

@Singleton
public class TeiValidator {
	private static final URL SCHEMA_RESOURCE = TeiValidator.class.getResource("/faust-tei.rng");

	private final XMLStorage xml;
	private final Logger logger;
	private final Schema schema;

	@Inject
	public TeiValidator(XMLStorage xml, Logger logger) throws IOException, SAXException, IncorrectSchemaException {
		this.xml = xml;
		this.logger = logger;

		final CustomErrorHandler errorHandler = new CustomErrorHandler();

		final PropertyMapBuilder builder = errorHandler.configurationWithErrorHandler();
		builder.put(ValidateProperty.XML_READER_CREATOR, new Sax2XMLReaderCreator());

		this.schema = SAXSchemaReader.getInstance().createSchema(new InputSource(SCHEMA_RESOURCE.toString()),
				builder.toPropertyMap());
		Preconditions.checkState(errorHandler.getErrors().isEmpty(), "No errors in schema");

		logger.info("Initialized RelaxNG-based TEI validator from " + SCHEMA_RESOURCE);
	}

	public List<String> validate(FaustURI uri) throws SAXException, IOException {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Validating via RelaxNG: " + uri);
		}
		final CustomErrorHandler errorHandler = new CustomErrorHandler();
		final Validator validator = schema.createValidator(errorHandler.configurationWithErrorHandler().toPropertyMap());

		final XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		xmlReader.setContentHandler(validator.getContentHandler());
		xmlReader.parse(xml.getInputSource(uri));

		return errorHandler.getErrors();
	}

	public boolean isValid(FaustURI uri) throws SAXException, IOException {
		return validate(uri).isEmpty();
	}

	private static class CustomErrorHandler implements ErrorHandler {
		private static final Pattern XMLNS_ATTR_URI_PATTERN = Pattern.compile(Pattern
				.quote(XMLConstants.XMLNS_ATTRIBUTE_NS_URI));

		private List<String> errors = new ArrayList<String>();

		private List<String> getErrors() {
			return errors;
		}

		private PropertyMapBuilder configurationWithErrorHandler() {
			PropertyMapBuilder builder = new PropertyMapBuilder();
			builder.put(ValidateProperty.ERROR_HANDLER, this);
			return builder;
		}

		/**
		 * Filters namespace declaration related errors.
		 * 
		 * @param e
		 */
		private void register(SAXParseException e) {
			if (!XMLNS_ATTR_URI_PATTERN.matcher(e.getMessage()).find()) {
				errors.add(String.format("[%d:%d] %s", e.getLineNumber(), e.getColumnNumber(), e.getMessage()));
			}
		}

		public void error(SAXParseException exception) throws SAXException {
			register(exception);
		}

		public void fatalError(SAXParseException exception) throws SAXException {
			register(exception);
		}

		public void warning(SAXParseException exception) throws SAXException {
			register(exception);
		}
	};
}
