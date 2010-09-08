package de.faustedition.metadata;

import static de.faustedition.xml.XPathUtil.xpath;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Element;

import de.faustedition.Log;
import de.faustedition.xml.NodeListIterable;
import de.faustedition.xml.XmlStore;

@Service
public class IdentifierManager implements Runnable {
	@Autowired
	private SimpleJdbcTemplate jt;

	@Autowired
	private XmlStore xmlStore;

	@Override
	@Transactional
	public void run() {
		Log.LOGGER.info("Updating identifier cache");

		final List<SqlParameterSource> identifierList = new ArrayList<SqlParameterSource>();
		for (Element identifier : new NodeListIterable<Element>(xpath("//f:id"), xmlStore.identifiers())) {
			MapSqlParameterSource identifierRecord = new MapSqlParameterSource();
			identifierRecord.addValue("path", identifier.getAttribute("path"));
			identifierRecord.addValue("type", identifier.getAttribute("type"));
			identifierRecord.addValue("id", identifier.getAttribute("value"));
			identifierList.add(identifierRecord);
		}

		final SqlParameterSource[] identifierArray = identifierList.toArray(new SqlParameterSource[identifierList.size()]);
		jt.update("delete from identifier");
		jt.batchUpdate("insert into identifier values (:path, :type, :id)", identifierArray);
	}
}
