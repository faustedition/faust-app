package de.faustedition.metadata;

import static de.faustedition.xml.XmlDocument.xpath;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
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
public class EncodingStatusManager {
	@Autowired
	private SimpleJdbcTemplate jt;

	@Autowired
	private XmlStore xmlStore;
	
	@Transactional(readOnly = true)
	public SortedMap<EncodingStatus, Integer> statusOf(String path) {
		final SortedMap<EncodingStatus, Integer> statusMap = new TreeMap<EncodingStatus, Integer>();
		jt.query("select encoding_status, count(*) as status_count from encoding_status "
				+ "where xml_path like ? group by encoding_status", new RowMapper<Void>() {

			public Void mapRow(ResultSet rs, int rowNum) throws SQLException {
				EncodingStatus status = EncodingStatus.valueOf(rs.getString("encoding_status"));
				statusMap.put(status, rs.getInt("status_count"));
				return null;
			}
		},  path + "%");
		return statusMap;
	}

	@Transactional
	public void update()  {
		Log.LOGGER.info("Updating encoding status cache ...");
		StopWatch sw = new StopWatch();
		sw.start();
		
		final List<SqlParameterSource> statusList = new ArrayList<SqlParameterSource>();
		for (Element statusEl : new NodeListIterable<Element>(xpath("//f:status"), xmlStore.encodingStati())) {
			MapSqlParameterSource status = new MapSqlParameterSource();
			status.addValue("path", statusEl.getAttribute("path"));
			status.addValue("status", EncodingStatus.valueOf(statusEl.getAttribute("value")).toString());
			statusList.add(status);
		}

		final SqlParameterSource[] statusArray = statusList.toArray(new SqlParameterSource[statusList.size()]);
		jt.update("delete from encoding_status");
		jt.batchUpdate("insert into encoding_status values (:path, :status)", statusArray);
		
		sw.stop();
		Log.LOGGER.info("Updated encoding status cache in {}", sw);
	}
}
