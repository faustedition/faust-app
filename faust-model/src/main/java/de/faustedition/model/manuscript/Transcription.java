package de.faustedition.model.manuscript;

import java.io.Serializable;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.support.DataAccessUtils;

public class Transcription implements Serializable {

	private long id;
	private Facsimile facsimile;
	private TranscriptionType transcriptionType;
	private byte[] data;

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

	public TranscriptionType getTranscriptionType() {
		return transcriptionType;
	}

	public void setTranscriptionType(TranscriptionType transcriptionType) {
		this.transcriptionType = transcriptionType;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && (obj instanceof Transcription) && (transcriptionType != null) && (facsimile != null)) {
			Transcription other = (Transcription) obj;
			if ((other.transcriptionType != null) && (other.facsimile != null)) {
				return transcriptionType.equals(other.transcriptionType) && (facsimile.getId() == other.facsimile.getId());
			}
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return (transcriptionType == null || facsimile == null) ? super.hashCode() : new HashCodeBuilder().append(transcriptionType).append(facsimile.getId()).toHashCode();
	}

	public static Transcription find(Session session, Facsimile facsimile, TranscriptionType type) {
		return (Transcription) DataAccessUtils.uniqueResult(session.createCriteria(Transcription.class).add(Restrictions.eq("transcriptionType", type)).createCriteria("facsimile").add(
				Restrictions.idEq(facsimile.getId())).list());
	}

	public static Transcription findOrCreate(Session session, Facsimile facsimile, TranscriptionType type) {
		Transcription transcription = find(session, facsimile, type);
		if (transcription == null) {
			transcription = new Transcription();
			transcription.setFacsimile(facsimile);
			transcription.setTranscriptionType(type);
			session.save(transcription);
		}

		return transcription;
	}

}
