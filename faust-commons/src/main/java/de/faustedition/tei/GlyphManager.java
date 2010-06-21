package de.faustedition.tei;

import static de.faustedition.tei.EncodedTextDocument.TEI_NS_URI;
import static de.faustedition.tei.EncodedTextDocument.xpath;
import static de.faustedition.xml.NodeListIterable.singleResult;

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

@Service
public class GlyphManager implements InitializingBean {

	@Autowired
	private SimpleJdbcTemplate jt;

	@Autowired
	private PlatformTransactionManager transactionManager;

	private SortedSet<Glyph> glyphs;

	@Override
	public void afterPropertiesSet() throws Exception {
		new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				SortedSet<Glyph> glyphs = get();
				if (jt.queryForInt("select count(*) from glyph") != glyphs.size()) {
					List<SqlParameterSource> glyphData = Lists.newArrayList();
					for (Glyph glyph : glyphs) {
						glyphData.add(glyph.toSqlParameterSource());
					}
					jt.update("delete from glyph");
					jt.batchUpdate("insert into glyph (id, name, description, equivalent) values (:id, :name, :description, :equivalent)",//
							glyphData.toArray(new SqlParameterSource[glyphData.size()]));
				}
			}

		});
	}

	public void declareIn(Document dom) {
		Element header = singleResult(xpath("//tei:teiHeader"), dom, Element.class);
		Assert.notNull(header, "No TEI header in document");

		Element encodingDesc = singleResult(xpath("./tei:encodingDesc"), header, Element.class);
		if (encodingDesc == null) {
			encodingDesc = dom.createElementNS(TEI_NS_URI, "encodingDesc");
			Element revisionDesc = singleResult(xpath("./tei:revisionDesc"), header, Element.class);
			header.insertBefore(encodingDesc, revisionDesc);
		}

		Element charDecl = singleResult(xpath("./tei:charDecl"), encodingDesc, Element.class);
		Node insertBefore = null;
		if (charDecl != null) {
			insertBefore = charDecl.getNextSibling();
			encodingDesc.removeChild(charDecl);
		}

		charDecl = dom.createElementNS(TEI_NS_URI, "charDecl");
		encodingDesc.insertBefore(charDecl, insertBefore);

		for (Glyph glyph : get()) {
			Element character = dom.createElementNS(TEI_NS_URI, "char");
			character.setAttributeNS(XMLConstants.XML_NS_URI, "xml:id", glyph.getId());
			charDecl.appendChild(character);

			Element charName = dom.createElementNS(TEI_NS_URI, "charName");
			charName.setTextContent(glyph.getName());
			character.appendChild(charName);

			Element description = dom.createElementNS(TEI_NS_URI, "desc");
			description.setTextContent(glyph.getDescription());
			character.appendChild(description);

			Element mapping = dom.createElementNS(TEI_NS_URI, "mapping");
			mapping.setAttribute("type", "Unicode");
			mapping.setTextContent(glyph.getEquivalent());
			character.appendChild(mapping);
		}
	}

	public synchronized SortedSet<Glyph> get() {
		if (glyphs == null) {
			glyphs = new TreeSet<Glyph>();
			glyphs.add(new Glyph("g_transp_1", "TRANSPOSITION SIGN 1", "„Einweisungzeichen“ mit einem nach rechts weisendem Querbalken.", null));
			glyphs.add(new Glyph("g_transp_2", "TRANSPOSITION SIGN 2", "„Einweisungzeichen“ mit zwei nach rechts weisendem Querbalken.", null));
			glyphs.add(new Glyph("g_transp_3", "TRANSPOSITION SIGN 3", "Geschwungenes „Einweisungzeichen“.", null));
			glyphs.add(new Glyph("g_transp_4", "TRANSPOSITION SIGN 4", "Gekreuztes „Einweisungzeichen“.", null));
			glyphs.add(new Glyph("parenthesis_left", "LEFT PARENTHESIS",//
					"In 18th century there were different ways to represent parentheses in handwritten documents. " + //
							"Then the token most often used (default symbol) was a vertical stroke combined with a colon. " + //
							"We want to be able to differentiate between this default representation, which we encode as normal " + //
							"parenthesis characters, and the very (same) characters, that are used today, and that also appear " + //
							"in documents of that time. Therefore we encode the contemporary representation of parantheses, if " + //
							"encountered in handwritten documents, as glyphs.", "("));
			glyphs.add(new Glyph("parenthesis_right", "RIGHT PARENTHESIS", "See the description of LEFT PARANTHESIS.", ")"));
			glyphs.add(new Glyph("truncation", "TRUNCATION SIGN", "The suspension/truncation sign, in German known as “Abbrechungszeichen”.", "."));
		}
		return glyphs;

	}
}
