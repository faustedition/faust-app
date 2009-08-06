package de.faustedition.model.metadata;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.support.DataAccessUtils;

import de.faustedition.model.HierarchyNodeType;
import de.faustedition.model.Model;

public class MetadataField extends Model {
	private MetadataFieldGroup group;
	private String name;
	private int order;
	private HierarchyNodeType lowestLevel;
	private MetadataFieldType type;

	public MetadataFieldGroup getGroup() {
		return group;
	}

	public void setGroup(MetadataFieldGroup group) {
		this.group = group;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public HierarchyNodeType getLowestLevel() {
		return lowestLevel;
	}

	public void setLowestLevel(HierarchyNodeType lowestLevel) {
		this.lowestLevel = lowestLevel;
	}

	public MetadataFieldType getType() {
		return type;
	}

	public void setType(MetadataFieldType type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object obj) {
		if ((obj != null) && getClass().equals(obj.getClass())) {
			return this.name.equalsIgnoreCase(((MetadataField) obj).name);
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return this.name.toUpperCase().hashCode();
	}

	public static boolean existsAny(Session session) {
		Criteria fieldCountCriteria = session.createCriteria(MetadataField.class).setProjection(Projections.rowCount());
		return DataAccessUtils.intResult(fieldCountCriteria.list()) > 0;
	}

	@SuppressWarnings("unchecked")
	public static MetadataField findByName(Session session, String fieldName) {
		return (MetadataField) DataAccessUtils.uniqueResult(session.createCriteria(MetadataField.class).add(Restrictions.eq("name", fieldName)).list());
	}

}
