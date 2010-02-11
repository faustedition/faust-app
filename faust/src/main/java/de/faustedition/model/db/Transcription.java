package de.faustedition.model.db;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.support.DataAccessUtils;
import org.xml.sax.SAXException;

import de.faustedition.model.tei.EncodedTextDocument;
import de.faustedition.model.tei.EncodedTextDocumentManager;
import de.faustedition.util.XMLUtil;

public class Transcription implements Serializable {

	private long id;
	private Facsimile facsimile;
	private Date created = new Date();
	private Date lastModified = new Date();
	private byte[] textData;
	private byte[] revisionData;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Facsimile getFacsimile() {
		return facsimile;
	}

	public void setFacsimile(Facsimile facsimile) {
		this.facsimile = facsimile;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public void modified() {
		setLastModified(new Date());
	}

	public byte[] getTextData() {
		return textData;
	}

	public void setTextData(byte[] data) {
		this.textData = data;
	}

	public boolean hasText() throws SAXException, IOException {
		return XMLUtil.hasText(XMLUtil.parse(getTextData()).getDocumentElement());
	}

	public byte[] getRevisionData() {
		return revisionData;
	}

	public void setRevisionData(byte[] revisionData) {
		this.revisionData = revisionData;
	}

	public void update(EncodedTextDocument document) {
		setTextData(XMLUtil.serializeFragment(document.getTextElement()));
		setRevisionData(XMLUtil.serializeFragment(document.getRevisionElement()));
	}

	public EncodedTextDocument buildTEIDocument(EncodedTextDocumentManager manager) {
		EncodedTextDocument teiDocument = manager.create();

		org.w3c.dom.Document dom = teiDocument.getDom();
		dom.getDocumentElement().appendChild(dom.importNode(XMLUtil.parse(getTextData()).getDocumentElement(), true));

		return teiDocument;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && (obj instanceof Transcription) && (facsimile != null)) {
			Transcription other = (Transcription) obj;
			if (other.facsimile != null) {
				return (facsimile.getId() == other.facsimile.getId());
			}
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return (facsimile == null ? super.hashCode() : new HashCodeBuilder().append(facsimile.getId()).toHashCode());
	}

	@SuppressWarnings("unchecked")
	public static Transcription find(Session session, Facsimile facsimile) {
		return DataAccessUtils.uniqueResult((List<Transcription>) session.createCriteria(Transcription.class)
				.createCriteria("facsimile").add(Restrictions.idEq(facsimile.getId())).list());
	}
}
