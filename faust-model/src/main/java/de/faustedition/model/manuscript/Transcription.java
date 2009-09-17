package de.faustedition.model.manuscript;

import java.io.Serializable;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.support.DataAccessUtils;

public class Transcription implements Serializable {

	private long id;
	private Facsimile facsimile;
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

	public byte[] getTextData() {
		return textData;
	}

	public void setTextData(byte[] data) {
		this.textData = data;
	}

	public byte[] getRevisionData() {
		return revisionData;
	}

	public void setRevisionData(byte[] revisionData) {
		this.revisionData = revisionData;
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

	public static Transcription find(Session session, Facsimile facsimile) {
		return (Transcription) DataAccessUtils.uniqueResult(session.createCriteria(Transcription.class).createCriteria("facsimile").add(Restrictions.idEq(facsimile.getId())).list());
	}
}
