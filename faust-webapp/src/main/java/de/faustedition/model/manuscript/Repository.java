package de.faustedition.model.manuscript;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.support.DataAccessUtils;

public class Repository implements Serializable
{
	private long id;
	private String name;

	public Repository()
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

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@SuppressWarnings("unchecked")
	public static Repository find(Session session, String name)
	{
		return DataAccessUtils.uniqueResult((List<Repository>) session.createCriteria(Repository.class).add(Restrictions.eq("name", name)).list());
	}

	public static Repository findOrCreate(Session session, String name)
	{
		Repository repository = find(session, name);
		if (repository == null)
		{
			repository = new Repository();
			repository.setName(name);
			session.save(repository);
		}
		return repository;
	}

	@SuppressWarnings("unchecked")
	public static List<Repository> find(Session session)
	{
		return session.createCriteria(Repository.class).addOrder(Order.asc("name")).list();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj != null && (name != null) && (obj instanceof Repository))
		{
			return name.equals(((Repository) obj).name);
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode()
	{
		return (name == null ? super.hashCode() : name.hashCode());
	}
}
