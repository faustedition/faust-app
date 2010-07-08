package de.faustedition.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/report/")
@Transactional(readOnly = true)
public class ReportController {
	@Autowired
	private SimpleJdbcTemplate jt;

	@RequestMapping("{name}")
	public String display(@PathVariable("name") String name, ModelMap model) {
		model.addAttribute(Report.get(jt, name));
		return "report";
	}
}
