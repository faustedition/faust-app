package de.faustedition.model.facsimile;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.support.DataAccessUtils;

import de.faustedition.model.db.Manuscript;

public class Facsimile implements Serializable
{
	private long id;
	private Manuscript manuscript;
	private String name;
	private String imagePath;

	public Facsimile()
	{
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public Manuscript getManuscript()
	{
		return manuscript;
	}

	public void setManuscript(Manuscript manuscript)
	{
		this.manuscript = manuscript;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getImagePath()
	{
		return imagePath;
	}

	public void setImagePath(String imagePath)
	{
		this.imagePath = imagePath;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj != null && (obj instanceof Facsimile) && (name != null) && (manuscript != null))
		{
			Facsimile other = (Facsimile) obj;
			if ((other.name != null) && (other.manuscript != null))
			{
				return name.equals(other.name) && (manuscript.getId() == other.manuscript.getId());
			}
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode()
	{
		return (name == null || manuscript == null) ? super.hashCode() : new HashCodeBuilder().append(name).append(manuscript.getId()).toHashCode();
	}

	@SuppressWarnings("unchecked")
	public static List<Facsimile> find(Session session, Manuscript manuscript)
	{
		return session.createCriteria(Facsimile.class).addOrder(Order.asc("name")).createCriteria("manuscript").add(Restrictions.idEq(manuscript.getId())).list();
	}

	@SuppressWarnings("unchecked")
	public static Facsimile find(Session session, Manuscript manuscript, String name)
	{
		return DataAccessUtils.uniqueResult((List<Facsimile>) session.createCriteria(Facsimile.class).add(Restrictions.eq("name", name)).createCriteria("manuscript").add(
				Restrictions.idEq(manuscript.getId())).list());
	}

	public static Facsimile findOrCreate(Session session, Manuscript manuscript, String name, String imagePath)
	{
		Facsimile facsimile = find(session, manuscript, name);
		if (facsimile == null)
		{
			facsimile = new Facsimile();
			facsimile.setManuscript(manuscript);
			facsimile.setName(name);
			facsimile.setImagePath(imagePath);
			session.save(facsimile);
		}

		return facsimile;
	}

	@SuppressWarnings("unchecked")
	public static Facsimile findByImagePath(Session session, String imagePath)
	{
		return DataAccessUtils.singleResult((List<Facsimile>) session.createCriteria(Facsimile.class).add(Restrictions.eq("imagePath", imagePath)).list());
	}

}
