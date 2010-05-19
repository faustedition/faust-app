package de.faustedition.tei;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import de.faustedition.report.Report;
import de.faustedition.report.ReportSender;
import de.faustedition.xml.XmlStore;

@Service
public class EncodedTextDocumentSanitizer {
	private static final String REPORT_SUBJECT = "TEI-Validierungsfehler";

	private static final Logger LOG = LoggerFactory.getLogger(EncodedTextDocumentSanitizer.class);

	@Autowired
	private XmlStore xmlStore;

	@Autowired
	private EncodedTextDocumentBuilder documentBuilder;
	
	@Autowired
	private ReportSender reportSender;

	@Autowired
	private EncodedTextDocumentValidator validator;

	public void sanitize() throws IOException {
		final Map<String, List<String>> errors = new LinkedHashMap<String, List<String>>();

		for (URI resourceUri : xmlStore.contents()) {
			if (!resourceUri.getPath().endsWith(".xml")) {
				continue;
			}
			try {
				LOG.debug("Sanitizing XML in {}", resourceUri.toString());
				EncodedTextDocument doc = new EncodedTextDocument((Document) xmlStore.get(resourceUri));
				documentBuilder.addTemplate(doc);
				xmlStore.put(resourceUri, doc.getDom());
				List<String> documentErrors = validator.validate(doc);
				if (!documentErrors.isEmpty()) {
					errors.put(resourceUri.toString(), documentErrors);
				}
			} catch (EncodedTextDocumentException e) {
				LOG.warn("Resource '{}' is not a TEI document", resourceUri.toString());
			}
			
		}

		reportSender.send(new Report() {

			public String getSubject() {
				return REPORT_SUBJECT;
			}

			public boolean isEmpty() {
				return errors.isEmpty();
			}

			public void printBody(PrintWriter body) {
				for (String path : errors.keySet()) {
					body.println(StringUtils.repeat("=", 78));
					body.println(path);
					body.println(StringUtils.repeat("-", 78));
					for (String e : errors.get(path)) {
						body.println(e);
					}
					body.println(StringUtils.repeat("=", 78));
					body.println();
				}
			}

		});

	}
}
