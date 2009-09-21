package de.faustedition.model.manuscript;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.support.DataAccessUtils;

public class Manuscript implements Serializable
{
	private long id;
	private Portfolio portfolio;
	private String name;

	public Manuscript()
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

	public Portfolio getPortfolio()
	{
		return portfolio;
	}

	public void setPortfolio(Portfolio portfolio)
	{
		this.portfolio = portfolio;
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
	public static List<Manuscript> find(Session session, Portfolio portfolio)
	{
		return session.createCriteria(Manuscript.class).addOrder(Order.asc("name")).createCriteria("portfolio").add(Restrictions.idEq(portfolio.getId())).list();
	}

	public static Manuscript find(Session session, Portfolio portfolio, String name)
	{
		return (Manuscript) DataAccessUtils.uniqueResult(session.createCriteria(Manuscript.class).add(Restrictions.eq("name", name)).createCriteria("portfolio").add(
				Restrictions.idEq(portfolio.getId())).list());
	}

	public static Manuscript findOrCreate(Session session, Portfolio portfolio, String name)
	{
		Manuscript manuscript = find(session, portfolio, name);
		if (manuscript == null)
		{
			manuscript = new Manuscript();
			manuscript.setPortfolio(portfolio);
			manuscript.setName(name);
			session.save(manuscript);
		}

		return manuscript;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj != null && (obj instanceof Manuscript) && (name != null) && (portfolio != null))
		{
			Manuscript other = (Manuscript) obj;
			if ((other.name != null) && (other.portfolio != null))
			{
				return name.equals(other.name) && (portfolio.getId() == other.portfolio.getId());
			}
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode()
	{
		return (name == null || portfolio == null) ? super.hashCode() : new HashCodeBuilder().append(name).append(portfolio.getId()).toHashCode();
	}
}
