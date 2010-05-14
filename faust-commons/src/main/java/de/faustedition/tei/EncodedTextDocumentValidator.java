package de.faustedition.tei;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.rng.SAXSchemaReader;
import com.thaiopensource.xml.sax.Sax2XMLReaderCreator;

import de.faustedition.ErrorUtil;
import de.faustedition.xml.XmlUtil;

@Service
public class EncodedTextDocumentValidator implements InitializingBean {
	private static final Logger LOG = LoggerFactory.getLogger(EncodedTextDocumentValidator.class);
	private static final String SCHEMA_RESOURCE = "/faust-tei.rng";
	private Schema schema;

	public boolean isValid(EncodedTextDocument document) {
		return validate(document).isEmpty();
	}

	public List<String> validate(EncodedTextDocument document) {
		try {
			final CustomErrorHandler errorHandler = new CustomErrorHandler();
			byte[] xml = XmlUtil.serialize(document.getDom());
			final ContentHandler validator = schema.createValidator(errorHandler.configurationWithErrorHandler().toPropertyMap()).getContentHandler();
			XmlUtil.saxParser().parse(new ByteArrayInputStream(xml), new DefaultHandler() {
				@Override
				public void setDocumentLocator(Locator locator) {
					validator.setDocumentLocator(locator);
				}

				@Override
				public void startDocument() throws SAXException {
					validator.startDocument();
				}

				@Override
				public void endDocument() throws SAXException {
					validator.endDocument();
				}

				@Override
				public void startPrefixMapping(String prefix, String uri) throws SAXException {
					validator.startPrefixMapping(prefix, uri);
				}

				@Override
				public void endPrefixMapping(String prefix) throws SAXException {
					validator.endPrefixMapping(prefix);
				}

				@Override
				public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
					validator.startElement(uri, localName, qName, attributes);
				}

				@Override
				public void endElement(String uri, String localName, String qName) throws SAXException {
					validator.endElement(uri, localName, qName);
				}

				@Override
				public void characters(char[] ch, int start, int length) throws SAXException {
					validator.characters(ch, start, length);
				}

				@Override
				public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
					validator.ignorableWhitespace(ch, start, length);
				}

				@Override
				public void processingInstruction(String target, String data) throws SAXException {
					validator.processingInstruction(target, data);
				}

				@Override
				public void skippedEntity(String name) throws SAXException {
					validator.skippedEntity(name);
				}
			});
			return errorHandler.getErrors();
		} catch (IOException e) {
			throw ErrorUtil.fatal(e, "I/O error while validating TEI document");
		} catch (SAXException e) {
			throw ErrorUtil.fatal(e, "XML error while validating TEI document");
		}
	}

	public URL getSchemaUrl() {
		return getClass().getResource(SCHEMA_RESOURCE);
	}

	public void afterPropertiesSet() throws Exception {
		final URL schemaUrl = getSchemaUrl();
		final InputSource schemaSource = new InputSource();
		schemaSource.setSystemId(schemaUrl.toExternalForm());
		schemaSource.setByteStream(schemaUrl.openStream());

		CustomErrorHandler errorHandler = new CustomErrorHandler();
		PropertyMapBuilder builder = errorHandler.configurationWithErrorHandler();
		builder.put(ValidateProperty.XML_READER_CREATOR, new Sax2XMLReaderCreator());
		schema = SAXSchemaReader.getInstance().createSchema(schemaSource, builder.toPropertyMap());
		Assert.isTrue(errorHandler.getErrors().isEmpty(), "No errors in schema");

		LOG.info("Initialized RelaxNG-based TEI validator from " + schemaUrl);
	}

	private static class CustomErrorHandler implements ErrorHandler {
		private static final Pattern XMLNS_ATTR_URI_PATTERN = Pattern.compile(Pattern.quote(XMLConstants.XMLNS_ATTRIBUTE_NS_URI));

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
