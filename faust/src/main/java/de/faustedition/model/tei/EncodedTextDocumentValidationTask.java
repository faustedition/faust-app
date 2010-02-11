package de.faustedition.model.tei;

import static de.faustedition.model.XmlDocument.xpath;

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
import org.w3c.dom.Element;

import de.faustedition.model.report.Report;
import de.faustedition.model.report.ReportSender;
import de.faustedition.model.xmldb.NodeListIterable;
import de.faustedition.model.xmldb.XmlDbManager;

@Service
public class EncodedTextDocumentValidationTask {
	private static final String REPORT_SUBJECT = "TEI-Validierungsfehler";

	private static final Logger LOG = LoggerFactory.getLogger(EncodedTextDocumentValidationTask.class);

	@Autowired
	private XmlDbManager xmlDbManager;

	@Autowired
	private ReportSender reportSender;

	@Autowired
	private EncodedTextDocumentValidator validator;

	public void validate() {
		final Map<String, List<String>> errors = new LinkedHashMap<String, List<String>>();

		Document resources = xmlDbManager.resources();
		for (Element resource : new NodeListIterable<Element>(xpath("//f:resource"), resources)) {
			String uri = resource.getTextContent();
			if (!uri.endsWith(".xml")) {
				continue;
			}
			try {
				EncodedTextDocument doc = new EncodedTextDocument((Document) xmlDbManager.get(URI.create(uri)));
				List<String> documentErrors = validator.validate(doc);
				if (!documentErrors.isEmpty()) {
					errors.put(uri, documentErrors);
				}
			} catch (EncodedTextDocumentException e) {
				LOG.warn("Resource '{}' is not a TEI document", uri);
			}
		}

		reportSender.send(new Report() {

			@Override
			public String getSubject() {
				return REPORT_SUBJECT;
			}

			@Override
			public boolean isEmpty() {
				return errors.isEmpty();
			}

			@Override
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
