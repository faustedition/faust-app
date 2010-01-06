package de.faustedition.web.schema;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

import de.faustedition.model.tei.EncodedDocument;

@Controller
@RequestMapping("/schema")
public class SchemaController {
	private static final Resource CSS_STYLESHEET_RESOURCE = new ClassPathResource("/css/faust-tei.css");
	private long startupTime = System.currentTimeMillis();

	@RequestMapping("/faust.rnc")
	public void streamRelaxSchema(WebRequest request, HttpServletResponse response) throws IOException {
		streamResource(request, response, EncodedDocument.RELAX_NG_SCHEMA_RESOURCE, "application/relax-ng-compact-syntax");
	}

	@RequestMapping("/faust.css")
	public void streamStylesheet(WebRequest request, HttpServletResponse response) throws IOException {
		streamResource(request, response, CSS_STYLESHEET_RESOURCE, "text/css");
	}

	private void streamResource(WebRequest request, HttpServletResponse response, Resource resource, String contentType)
			throws IOException {
		if (request.checkNotModified(startupTime)) {
			return;
		}

		response.setContentType(contentType);
		response.setCharacterEncoding("UTF-8");
		ServletOutputStream outputStream = response.getOutputStream();
		IOUtils.copy(resource.getInputStream(), outputStream);
		outputStream.flush();
	}
}
