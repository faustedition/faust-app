package de.faustedition;

import com.google.common.base.Throwables;
import eu.interedition.text.json.map.TextModule;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.io.Writer;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Service
public class JsonRepresentationFactory {

	@Autowired
	private TransactionTemplate transactionTemplate;

	private final ObjectMapper objectMapper = new ObjectMapper();

	public JsonRepresentationFactory() {
		this.objectMapper.registerModule(new TextModule());
	}

	public Representation map(Object object) {
		return map(object, true);
	}

	public Representation map(final Object object, final boolean transactional) {
		return new WriterRepresentation(MediaType.APPLICATION_JSON) {
			@Override
			public void write(final Writer writer) throws IOException {
				if (transactional) {
					try {
						transactionTemplate.execute(new TransactionCallbackWithoutResult() {
							@Override
							protected void doInTransactionWithoutResult(TransactionStatus status) {
								try {
									writeJson(writer);
								} catch (IOException e) {
									throw Throwables.propagate(e);
								}
							}
						});
					} catch (Throwable t) {
						Throwables.propagateIfInstanceOf(Throwables.getRootCause(t), IOException.class);
						throw Throwables.propagate(t);
					}
				} else {
					writeJson(writer);
				}

			}

			protected void writeJson(Writer writer) throws IOException {
				final JsonGenerator jg = objectMapper.getJsonFactory().createJsonGenerator(writer);
				jg.writeObject(object);
				jg.flush();
			}
		};
	}
}
