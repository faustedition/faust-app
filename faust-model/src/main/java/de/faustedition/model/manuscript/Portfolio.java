package de.faustedition.model.manuscript;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.support.DataAccessUtils;

public class Portfolio implements Serializable {

	private long id;
	private Repository repository;
	private String name;

	public Portfolio() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static Portfolio find(Session session, Repository repository, String name) {
		return (Portfolio) DataAccessUtils.uniqueResult(session.createCriteria(Portfolio.class).add(Restrictions.eq("name", name)).createCriteria("repository").add(
				Restrictions.idEq(repository.getId())).list());
	}

	public static Portfolio findOrCreate(Session session, Repository repository, String name) {
		Portfolio portfolio = find(session, repository, name);
		if (portfolio == null) {
			portfolio = new Portfolio();
			portfolio.setRepository(repository);
			portfolio.setName(name);
			session.save(portfolio);
		}

		return portfolio;
	}

	@SuppressWarnings("unchecked")
	public static List<Portfolio> find(Session session, Repository repository) {
		return session.createCriteria(Portfolio.class).addOrder(Order.asc("name")).createCriteria("repository").add(Restrictions.idEq(repository.getId())).list();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && (obj instanceof Portfolio) && (name != null) && (repository != null)) {
			Portfolio other = (Portfolio) obj;
			if ((other.name != null) && (other.repository != null)) {
				return name.equals(other.name) && (repository.getId() == other.repository.getId());
			}
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return (name == null || repository == null) ? super.hashCode() : new HashCodeBuilder().append(name).append(repository.getId()).toHashCode();
	}
}
