package de.faustedition.tei;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.thaiopensource.util.PropertyMapBuilder;
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
import org.apache.commons.mail.EmailException;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.XMLConstants;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class TeiValidator extends Runtime implements Runnable, InitializingBean {
	private static final URL SCHEMA_RESOURCE = TeiValidator.class.getResource("/faust-tei.rng");

	@Autowired
	private XMLStorage xml;

	@Autowired
	private Logger logger;

	@Autowired
	private EmailReporter reporter;

	private Schema schema;

	@Override
	public void afterPropertiesSet() throws Exception {
		final CustomErrorHandler errorHandler = new CustomErrorHandler();

		final PropertyMapBuilder builder = errorHandler.configurationWithErrorHandler();
		builder.put(ValidateProperty.XML_READER_CREATOR, new Sax2XMLReaderCreator());

		this.schema = SAXSchemaReader.getInstance().createSchema(new InputSource(SCHEMA_RESOURCE.toString()),
			builder.toPropertyMap());
		Preconditions.checkState(errorHandler.getErrors().isEmpty(), "No errors in schema");

		logger.info("Initialized RelaxNG-based TEI validator from " + SCHEMA_RESOURCE);
	}

	public List<String> validate(FaustURI uri) throws SAXException, IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("Validating via RelaxNG: " + uri);
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

	public static void main(String[] args) throws IOException {
		main(TeiValidator.class, args);
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
					logger.debug("XML error while validating transcript: " + source, e);
					xmlErrors.add(source);
				} catch (IOException e) {
					logger.warn("I/O error while validating transcript: " + source, e);
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
