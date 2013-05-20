package de.faustedition.tei;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Guice;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.rng.SAXSchemaReader;
import com.thaiopensource.xml.sax.Sax2XMLReaderCreator;
import de.faustedition.DataModule;
import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.xml.XMLStorage;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.inject.Inject;
import javax.xml.XMLConstants;
import java.io.File;
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

    private final XMLStorage xml;
    private final Logger logger;

    @Inject
    public TeiValidator(XMLStorage xml, Logger logger) {
        this.xml = xml;
        this.logger = logger;
    }

    public static void main(String[] args) {
        try {
            final CommandLine commandLine = new GnuParser().parse(OPTIONS, args);
            if (commandLine.hasOption("h")) {
                new HelpFormatter().printHelp(78, "faust-server [<options>]", "", OPTIONS, "");
                return;
            }

            final File dataDirectory = new File(commandLine.getOptionValue("d", "data"));
            final URI schema = commandLine.hasOption("s") ? URI.create(commandLine.getOptionValue("s")) : null;

            Preconditions.checkArgument(dataDirectory.isDirectory(), dataDirectory + " is not a directory");
            Preconditions.checkArgument(schema != null, "No schema URI given");

            Guice.createInjector(new DataModule(dataDirectory)).getInstance(TeiValidator.class).validate(schema);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void validate(URI schemaSource) throws SAXException, IOException, IncorrectSchemaException {
        final CustomErrorHandler errorHandler = new CustomErrorHandler();

        final PropertyMapBuilder builder = errorHandler.configurationWithErrorHandler();
        builder.put(ValidateProperty.XML_READER_CREATOR, new Sax2XMLReaderCreator());

        final Schema schema = SAXSchemaReader.getInstance().createSchema(new InputSource(schemaSource.toString()), builder.toPropertyMap());
        Preconditions.checkState(errorHandler.getErrors().isEmpty(), "No errors in schema");

        logger.info("Initialized RelaxNG-based TEI validator from " + schemaSource);

        final SortedSet<FaustURI> xmlErrors = new TreeSet<FaustURI>();
        final SortedMap<FaustURI, String> teiErrors = new TreeMap<FaustURI, String>();
        for (FaustURI source : xml.iterate(new FaustURI(FaustAuthority.XML, "/transcript"))) {
            try {
                final List<String> errors = validate(schema, source);
                if (!errors.isEmpty()) {
                    teiErrors.put(source, Joiner.on("\n").join(errors));
                }
            } catch (SAXException e) {
                logger.log(Level.SEVERE, "XML error while validating transcript: " + source, e);
                xmlErrors.add(source);
            } catch (IOException e) {
                logger.log(Level.WARNING, "I/O error while validating transcript: " + source, e);
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

    public List<String> validate(Schema schema, FaustURI uri) throws SAXException, IOException {
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

    public boolean isValid(Schema schema, FaustURI uri) throws SAXException, IOException {
        return validate(schema, uri).isEmpty();
    }


    static final Options OPTIONS = new Options();

    static {
        OPTIONS.addOption("h", "help", false, "print usage instructions");
        OPTIONS.addOption("s", "schema", true, "Schema URI");
        OPTIONS.addOption("d", "data", true, "Path to data directory; default: 'data'");
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

    ;
}
