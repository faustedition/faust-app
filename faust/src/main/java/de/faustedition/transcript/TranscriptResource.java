package de.faustedition.transcript;

import org.hibernate.SessionFactory;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.NotFoundException;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Objects;

import de.faustedition.document.MaterialUnit;
import de.faustedition.template.TemplateRepresentationFactory;
import de.faustedition.xml.XMLStorage;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class TranscriptResource extends ServerResource {
	@Autowired
	protected TemplateRepresentationFactory templateFactory;

	@Autowired
	protected GraphDatabaseService db;

	@Autowired
	protected SessionFactory sessionFactory;

	@Autowired
	protected XMLStorage xml;

	protected MaterialUnit materialUnit;

	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		final String nodeId = Objects.firstNonNull((String) getRequest().getAttributes().get("id"), "-1");
		try {
			this.materialUnit = MaterialUnit.forNode(db.getNodeById(Long.parseLong(nodeId)));
			if (materialUnit.getTranscriptSource() == null) {
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, materialUnit.toString());
			}
		} catch (NumberFormatException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		} catch (ClassCastException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		} catch (NotFoundException e) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, nodeId);
		}
	}

}
