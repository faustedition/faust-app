package de.faustedition.transcript;

import com.google.common.io.CharStreams;
import de.faustedition.AbstractContextTest;
import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.xml.XMLStorage;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TranscriptTest extends AbstractContextTest {

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private XMLStorage xml;

	@Test
	public void read() throws XMLStreamException, IOException {
		final Transcript transcript = Transcript.find(sessionFactory.getCurrentSession(), xml, new FaustURI(FaustAuthority.XML, "/transcript/gsa/391098/0005.xml"));
		CharStreams.copy(transcript.getText().read(), System.out);
	}
}
