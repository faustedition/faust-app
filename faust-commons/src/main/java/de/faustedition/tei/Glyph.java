package de.faustedition.tei;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public class Glyph implements Comparable<Glyph> {
	private String id;
	private String name;
	private String description;
	private String equivalent;

	public Glyph(String id, String name, String description, String equivalent) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.equivalent = equivalent;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getEquivalent() {
		return equivalent;
	}

	public MapSqlParameterSource toSqlParameterSource() {
		MapSqlParameterSource ps = new MapSqlParameterSource();
		ps.addValue("id", id);
		ps.addValue("name", name);
		ps.addValue("description", description);
		ps.addValue("equivalent", equivalent);
		return ps;
	}

	public static final RowMapper<Glyph> ROW_MAPPER = new RowMapper<Glyph>() {

		@Override
		public Glyph mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new Glyph(rs.getString("id"), rs.getString("name"), rs.getString("description"), rs.getString("equivalent"));
		}
	};

	@Override
	public int compareTo(Glyph o) {
		return id.compareTo(o.id);
	}
}
