package de.faustedition.model.document;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;

import de.faustedition.model.hierarchy.HierarchyNode;
import de.faustedition.model.hierarchy.HierarchyNodeFacet;
import de.faustedition.model.tei.EncodedTextDocument;
import de.faustedition.util.HibernateUtil;
import de.faustedition.util.XMLUtil;

public class TranscriptionDocument extends HierarchyNodeFacet {
	private byte[] documentData;
	private Date created = new Date();
	private Date lastModified = new Date();
	private TranscriptionStatus status;

	public byte[] getDocumentData() {
		return documentData;
	}

	public String getDocumentString() {
		return new String(documentData, Charset.forName("UTF-8"));
	}

	public EncodedTextDocument getTeiDocument() {
		return (documentData == null ? null : new EncodedTextDocument(XMLUtil.parse(documentData)));
	}

	public void setDocumentData(byte[] documentData) {
		this.documentData = documentData;
	}

	public void setDocumentData(EncodedTextDocument teiDocument) {
		setDocumentData(XMLUtil.serialize(teiDocument.getDocument(), false));
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

	public TranscriptionStatus getStatus() {
		return status;
	}

	public void setStatus(TranscriptionStatus status) {
		this.status = status;
	}

	public void updateStatus() {
		setStatus(TranscriptionStatus.extract(getTeiDocument()));
	}

	public static Iterable<TranscriptionDocument> scrollAll(Session session) {
		return HibernateUtil.scroll(session.createCriteria(TranscriptionDocument.class), TranscriptionDocument.class);
	}

	public static SortedMap<TranscriptionStatus, Integer> summarizeTranscriptionStatus(Session session, HierarchyNode node) {
		ProjectionList projectionList = Projections.projectionList();
		projectionList.add(Projections.groupProperty("status"));
		projectionList.add(Projections.rowCount());

		Criteria facetCriteria = session.createCriteria(TranscriptionDocument.class).setProjection(projectionList);
		node.addDescendantCriteria(facetCriteria.createCriteria("facettedNode"));

		SortedMap<TranscriptionStatus, Integer> summary = new TreeMap<TranscriptionStatus, Integer>();
		for (Object[] transcriptionStats : HibernateUtil.scroll(facetCriteria, Object[].class)) {
			summary.put((TranscriptionStatus) transcriptionStats[0], (Integer) transcriptionStats[1]);
		}

		return summary;
	}
}
