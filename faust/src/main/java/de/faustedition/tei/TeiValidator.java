package de.faustedition.tei;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;

import org.apache.commons.mail.EmailException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.rng.SAXSchemaReader;
import com.thaiopensource.xml.sax.Sax2XMLReaderCreator;

import de.faustedition.EmailReporter;
import de.faustedition.EmailReporter.ReportCreator;
import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.Runtime;
import de.faustedition.xml.XMLStorage;

@Singleton
public class TeiValidator extends Runtime implements Runnable {
	private static final URL SCHEMA_RESOURCE = TeiValidator.class.getResource("/faust-tei.rng");

	private final XMLStorage xml;
	private final Logger logger;
	private final Schema schema;
	private final EmailReporter reporter;

	@Inject
	public TeiValidator(XMLStorage xml, EmailReporter reporter, Logger logger) throws IOException, SAXException,
			IncorrectSchemaException {
		this.xml = xml;
		this.reporter = reporter;
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

	public static void main(String[] args) {
		main(TeiValidator.class, args);
		System.exit(0);
	}

	@Override
	public void run() {
		try {
			final SortedSet<FaustURI> xmlErrors = new TreeSet<FaustURI>();
			final SortedMap<FaustURI, String> teiErrors = new TreeMap<FaustURI, String>();
			for (FaustURI source : xml.iterate(new FaustURI(FaustAuthority.XML, "/transcript"))) {
				try {
					final List<String> errors = validate(source);
					if (!errors.isEmpty()) {
						teiErrors.put(source, Joiner.on("\n").join(errors));
					}
				} catch (SAXException e) {
					logger.log(Level.FINE, "XML error while validating transcript: " + source, e);
					xmlErrors.add(source);
				} catch (IOException e) {
					logger.log(Level.WARNING, "I/O error while validating transcript: " + source, e);
				}
			}

			if (xmlErrors.isEmpty() && teiErrors.isEmpty()) {
				return;
			}

			reporter.send("TEI validation report", new ReportCreator() {

				public void create(PrintWriter body) {
					if (!xmlErrors.isEmpty()) {
						body.println(Strings.padStart(" XML errors", 79, '='));
						body.println();
						body.println(Joiner.on("\n").join(xmlErrors));
						body.println();
					}
					if (!teiErrors.isEmpty()) {
						body.println(Strings.padStart(" TEI errors", 79, '='));
						body.println();

						for (Map.Entry<FaustURI, String> teiError : teiErrors.entrySet()) {
							body.println(Strings.padStart(" " + teiError.getKey(), 79, '-'));
							body.println();
							body.println(teiError.getValue());
							body.println();
							body.println();
						}
					}
				}
			});
		} catch (EmailException e) {
			e.printStackTrace();
		}
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
