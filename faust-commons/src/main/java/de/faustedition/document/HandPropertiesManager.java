package de.faustedition.document;

import static de.faustedition.tei.EncodedTextDocument.TEI_NS_URI;
import static de.faustedition.tei.EncodedTextDocument.xpath;
import static de.faustedition.xml.NodeListIterable.singleResult;

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.XMLConstants;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.faustedition.tei.EncodedTextDocument;

@Service
public class HandPropertiesManager implements InitializingBean {

	@Autowired
	private SimpleJdbcTemplate jt;

	@Autowired
	private PlatformTransactionManager transactionManager;

	private SortedSet<HandProperties> handProperties;

	public void afterPropertiesSet() throws Exception {
		new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				if (jt.queryForInt("select count(*) from hand") == 0) {
					List<SqlParameterSource> handData = Lists.newArrayList();
					for (HandProperties hand : createHands()) {
						handData.add(hand.toSqlParameterSource());
					}
					jt.batchUpdate("insert into hand (scribe, material, style) values (:scribe, :material, :style)",//
							handData.toArray(new SqlParameterSource[handData.size()]));
				}
			}
		});
	}

	public void declareIn(Document dom) {
		Element header = singleResult(xpath("//tei:teiHeader"), dom, Element.class);
		Assert.notNull(header, "No TEI header in document");

		Element profileDesc = singleResult(xpath("./tei:profileDesc"), header, Element.class);
		if (profileDesc == null) {
			profileDesc = dom.createElementNS(TEI_NS_URI, "profileDesc");
			Element revisionDesc = singleResult(xpath("./tei:revisionDesc"), header, Element.class);
			header.insertBefore(profileDesc, revisionDesc);
		}

		Element handNotes = singleResult(xpath("./tei:handNotes"), profileDesc, Element.class);
		Node insertBefore = null;
		if (handNotes != null) {
			insertBefore = handNotes.getNextSibling();
			profileDesc.removeChild(handNotes);
		}

		handNotes = dom.createElementNS(EncodedTextDocument.TEI_NS_URI, "handNotes");
		for (HandProperties hp : get()) {
			Element hn = dom.createElementNS(EncodedTextDocument.TEI_NS_URI, "handNote");
			hn.setAttributeNS(XMLConstants.XML_NS_URI, "xml:id", hp.getKey());
			hn.setTextContent(hp.getDescription());
			handNotes.appendChild(hn);
		}
		profileDesc.insertBefore(handNotes, insertBefore);
	}

	public synchronized SortedSet<HandProperties> get() {
		if (handProperties == null) {
			new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {

				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					handProperties = new TreeSet<HandProperties>(jt.query("select * from hand", HandProperties.ROW_MAPPER));
				}
			});
		}

		return handProperties;
	}

	public List<HandProperties> createHands() {
		List<HandProperties> hands = Lists.newArrayList();

		SortedSet<Scribe> scribes = Sets.newTreeSet(Lists.newArrayList(Scribe.values()));
		SortedSet<WritingMaterial> materials = Sets.newTreeSet(Lists.newArrayList(WritingMaterial.values()));
		materials.remove(WritingMaterial.BLUE);

		List<FontStyle> styles = Arrays.asList(FontStyle.values());

		// Goethe
		for (WritingMaterial material : materials) {
			for (FontStyle style : styles) {
				hands.add(new HandProperties(Scribe.GOETHE, material, style));
			}
		}
		scribes.remove(Scribe.GOETHE);

		materials.remove(WritingMaterial.CHARCOAL);
		materials.remove(WritingMaterial.RUDDLE);

		// Goethe's scribes
		SortedSet<Scribe> other = Sets.newTreeSet(Sets.newHashSet(//
				Scribe.CONTEMPORARY,//
				Scribe.UNKNOWN_SCRIBE_1,//
				Scribe.UNKNOWN_SCRIBE_2,//
				Scribe.UNKNOWN_SCRIBE_3));
		scribes.removeAll(other);

		for (Scribe scribe : scribes) {
			for (WritingMaterial material : materials) {
				for (FontStyle style : styles) {
					hands.add(new HandProperties(scribe, material, style));
				}
			}
		}

		materials.add(WritingMaterial.BLUE);

		// misc. scribes
		for (Scribe scribe : other) {
			for (WritingMaterial material : materials) {
				for (FontStyle style : styles) {
					hands.add(new HandProperties(scribe, material, style));
				}
			}
		}

		return hands;
	}
}
