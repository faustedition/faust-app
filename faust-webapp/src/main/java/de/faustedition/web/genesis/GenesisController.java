package de.faustedition.web.genesis;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.AbstractView;

import com.google.common.collect.Lists;

@Controller
public class GenesisController
{
	private static final List<ParalipomenonReference> PARALIPOMENA_REFS = Lists.newArrayList(new ParalipomenonReference("P195", "391082", "0002"), new ParalipomenonReference("P21", "390782",
			"0002"), new ParalipomenonReference("P1", "390720", "0002"), new ParalipomenonReference("P93/P95", "390882", "0002"), new ParalipomenonReference("P91", "391314", "0002"),
			new ParalipomenonReference("P92a", "390781", "0002"), new ParalipomenonReference("P92b", "390826", "0002"), new ParalipomenonReference("P96", "390050", "0002"),
			new ParalipomenonReference("P97", "390777", "0002"), new ParalipomenonReference("P98a", "390705", "0002"), new ParalipomenonReference("P98b", "390705", "0003"));

	private static final ParalipomenonReference URFAUST_REF = new ParalipomenonReference("Urfaust-Schluss", "390028", "0095");

	@RequestMapping("/genesis/")
	public String overview(HttpServletRequest request, ModelMap model) throws IOException
	{
		StringWriter imageMap = new StringWriter();
		new GenesisExampleChart().render(new ByteArrayOutputStream(), new PrintWriter(imageMap), request.getContextPath() + "/manuscripts/", "genesisChart");

		model.addAttribute("imageMap", imageMap.toString());
		model.addAttribute("paralipomena", PARALIPOMENA_REFS);
		model.addAttribute("urfaust", URFAUST_REF);
		
		return "genesis";
	}

	@RequestMapping("/genesis/chart.png")
	public ModelAndView renderChart() throws IOException
	{
		return new ModelAndView(new AbstractView()
		{

			@SuppressWarnings("unchecked")
			@Override
			protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception
			{
				response.setContentType("image/png");
				ServletOutputStream outputStream = response.getOutputStream();
				new GenesisExampleChart().render(outputStream, new PrintWriter(new StringWriter()), "genesisChart", "");
				outputStream.flush();
			}
		});
	}
}
