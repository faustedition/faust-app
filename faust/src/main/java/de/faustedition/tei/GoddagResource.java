package de.faustedition.tei;

import static org.restlet.data.MediaType.APPLICATION_XML;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;

import org.goddag4j.Element;
import org.goddag4j.MultiRootedTree;
import org.goddag4j.io.GoddagJSONWriter;
import org.goddag4j.io.GoddagJSONWriter.GoddagJSONEnhancer;
import org.goddag4j.io.GoddagXMLWriter;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.restlet.data.Form;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Throwables;

import de.faustedition.FaustURI;
import de.faustedition.JsonRespresentation;
import de.faustedition.transcript.TranscriptType;
import de.faustedition.xml.CustomNamespaceMap;
import de.faustedition.xml.XMLUtil;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GoddagResource extends ServerResource {

	@Autowired
	protected GraphDatabaseService db;

	protected FaustURI source;
	protected TranscriptType transcriptType;
	protected MultiRootedTree trees;
	protected GoddagJSONEnhancer enhancer = GoddagJSONWriter.NOOP_ENHANCER;

	public void setSource(FaustURI source) {
		this.source = source;
	}

	public void setTranscriptType(TranscriptType transcriptType) {
		this.transcriptType = transcriptType;
	}

	public void setTrees(MultiRootedTree trees) {
		this.trees = trees;
	}

	public void setEnhancer(GoddagJSONEnhancer enhancer) {
		this.enhancer = enhancer;
	}

	public MultiRootedTree trees() {
		return trees;
	}

	@Get("json")
	public Representation streamJson() {
		return new JsonRespresentation() {

			@Override
			protected void generate() throws IOException {
				try {
					inTransaction(new InTransactionCallback() {

						@Override
						public void inTransaction() throws Exception {
							new GoddagJSONWriter(CustomNamespaceMap.INSTANCE, enhancer).write(trees(),
									generator);
						}
					});
				} catch (Exception e) {
					Throwables.propagateIfInstanceOf(e, IOException.class);
					throw Throwables.propagate(e);
				}
			}
		};
	}

	@Get("xml")
	public Representation streamXML() {
		final Form parameters = getRequest().getResourceRef().getQueryAsForm();
		final String rootQName = parameters.getFirstValue("root");
		final MultiRootedTree trees = trees();

		String rootPrefix = null;
		String rootLocalName = null;
		final int lcColonIndex = rootQName == null ? -1 : rootQName.indexOf(':');
		if (lcColonIndex > 0 && (lcColonIndex + 1) < rootQName.length()) {
			rootPrefix = rootQName.substring(0, lcColonIndex);
			rootLocalName = rootQName.substring(lcColonIndex + 1);
		}

		Element rootCandidate = null;
		if (rootPrefix != null && rootLocalName != null) {
			rootCandidate = trees.findRoot(rootPrefix, rootLocalName);
		} else {
			final Iterator<Element> rootIt = trees.iterator();
			rootCandidate = (rootIt.hasNext() ? rootIt.next() : null);
		}

		if (rootCandidate == null) {
			return null;
		}

		final Element root = rootCandidate;
		final boolean showTextNodes = Boolean.valueOf(parameters.getFirstValue("textnodes", "false"));
		return new OutputRepresentation(APPLICATION_XML) {

			@Override
			public void write(final OutputStream outputStream) throws IOException {
				try {
					inTransaction(new InTransactionCallback() {

						@Override
						public void inTransaction() throws Exception {
							Transformer transformer = XMLUtil.transformerFactory().newTransformer();
							final Source source = new GoddagXMLWriter(root,
									CustomNamespaceMap.INSTANCE, showTextNodes).toSAXSource();
							transformer.transform(source, new StreamResult(outputStream));
						}
					});
				} catch (Exception e) {
					Throwables.propagateIfInstanceOf(e, IOException.class);
					throw Throwables.propagate(e);
				}
			}
		};
	}

	protected void inTransaction(InTransactionCallback callback) throws Exception {
		Transaction tx = db.beginTx();
		try {
			callback.inTransaction();
			tx.success();
		} finally {
			tx.finish();
		}
	}

	protected interface InTransactionCallback {
		void inTransaction() throws Exception;
	}
}
