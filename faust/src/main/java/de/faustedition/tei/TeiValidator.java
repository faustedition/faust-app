package de.faustedition.tei;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.rng.SAXSchemaReader;
import com.thaiopensource.xml.sax.Sax2XMLReaderCreator;
import dagger.ObjectGraph;
import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.ServerModule;
import de.faustedition.xml.XMLStorage;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.XMLConstants;
import java.io.IOException;
import java.net.URI;
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

public class TeiValidator {

    private static final Logger LOG = Logger.getLogger(TeiValidator.class.getName());

    public static void main(String[] args) {
        try {
            final ServerModule serverModule = ServerModule.fromCommandLineArgs(args);
            validate(
                    URI.create(serverModule.getConfiguration().property("faust.schema_uri")),
                    ObjectGraph.create(serverModule).get(XMLStorage.class)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void validate(URI schemaSource, XMLStorage xmlStorage) throws SAXException, IOException, IncorrectSchemaException {
        final CustomErrorHandler errorHandler = new CustomErrorHandler();

        final PropertyMapBuilder builder = errorHandler.configurationWithErrorHandler();
        builder.put(ValidateProperty.XML_READER_CREATOR, new Sax2XMLReaderCreator());

        final Schema schema = SAXSchemaReader.getInstance().createSchema(new InputSource(schemaSource.toString()), builder.toPropertyMap());
        Preconditions.checkState(errorHandler.getErrors().isEmpty(), "No errors in schema");

        LOG.info("Initialized RelaxNG-based TEI validator from " + schemaSource);

        final SortedSet<FaustURI> xmlErrors = new TreeSet<FaustURI>();
        final SortedMap<FaustURI, String> teiErrors = new TreeMap<FaustURI, String>();
        for (FaustURI source : xmlStorage.iterate(new FaustURI(FaustAuthority.XML, "/transcript"))) {
            try {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Validating via RelaxNG: " + source);
                }
                final List<String> errors = validate(schema, xmlStorage.getInputSource(source));
                if (!errors.isEmpty()) {
                    teiErrors.put(source, Joiner.on("\n").join(errors));
                }
            } catch (SAXException e) {
                LOG.log(Level.SEVERE, "XML error while validating transcript: " + source, e);
                xmlErrors.add(source);
            } catch (IOException e) {
                LOG.log(Level.WARNING, "I/O error while validating transcript: " + source, e);
            }
        }

        if (xmlErrors.isEmpty() && teiErrors.isEmpty()) {
            return;
        }

        if (!xmlErrors.isEmpty()) {
            System.out.println(Strings.padStart(" XML errors", 79, '='));
            System.out.println();
            System.out.println(Joiner.on("\n").join(xmlErrors));
            System.out.println();
        }
        if (!teiErrors.isEmpty()) {
            System.out.println(Strings.padStart(" TEI errors", 79, '='));
            System.out.println();

            for (Map.Entry<FaustURI, String> teiError : teiErrors.entrySet()) {
                System.out.println(Strings.padStart(" " + teiError.getKey(), 79, '-'));
                System.out.println();
                System.out.println(teiError.getValue());
                System.out.println();
                System.out.println();
            }
        }
    }

    public static List<String> validate(Schema schema, InputSource xml) throws SAXException, IOException {
        final CustomErrorHandler errorHandler = new CustomErrorHandler();
        final Validator validator = schema.createValidator(errorHandler.configurationWithErrorHandler().toPropertyMap());

        final XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        xmlReader.setContentHandler(validator.getContentHandler());
        xmlReader.parse(xml);

        return errorHandler.getErrors();
    }

    public boolean isValid(Schema schema, InputSource xml) throws SAXException, IOException {
        return validate(schema, xml).isEmpty();
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
    }
}
