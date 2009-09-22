package de.faustedition.web.genesis;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class GenesisGanttChartController
{
	@RequestMapping("/chart/genesis.png")
	public void renderChart(HttpServletResponse response) throws Exception
	{
		response.setContentType("image/png");
		ServletOutputStream responseStream = response.getOutputStream();
		new GenesisExampleChart().render(responseStream, new PrintWriter(new StringWriter()), "genesisChart");
		responseStream.flush();
	}
}