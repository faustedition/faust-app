package de.faustedition.report;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

public class Report {
	private String name;
	private Date generatedOn;
	private String body;

	public Report() {
	}

	public Report(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getGeneratedOn() {
		return generatedOn;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public boolean isEmpty() {
		return body == null;
	}

	public MapSqlParameterSource toSqlParameterSource() {
		MapSqlParameterSource ps = new MapSqlParameterSource();
		ps.addValue("name", name);
		ps.addValue("generated_on", new Date());
		ps.addValue("body", body);
		return ps;
	}

	public static Report get(SimpleJdbcTemplate jt, String name) {
		return DataAccessUtils.requiredUniqueResult(jt.query("select * from report where name = ?", ROW_MAPPER, name));
	}

	public static boolean exists(SimpleJdbcTemplate jt, String name) {
		return jt.queryForInt("select count(*) from report where name = ?", name) > 0;
	}

	public void save(SimpleJdbcTemplate jt) {
		MapSqlParameterSource ps = toSqlParameterSource();
		if (exists(jt, name)) {
			jt.update("update report set generated_on = :generated_on, body = :body where name = :name", ps);
		} else {
			jt.update("insert into report (name, generated_on, body) values (:name, :generated_on, :body)", ps);
		}
	}

	private final static RowMapper<Report> ROW_MAPPER = new RowMapper<Report>() {

		@Override
		public Report mapRow(ResultSet rs, int rowNum) throws SQLException {
			Report report = new Report();
			report.name = rs.getString("name");
			report.generatedOn = rs.getTimestamp("generated_on");
			report.body = rs.getString("body");
			return report;
		}
	};
}
