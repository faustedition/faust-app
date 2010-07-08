package de.faustedition.tei;

import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;


@Controller
@RequestMapping("/schema")
public class SchemaController {
	private final long CONTROLLER_CREATION_TIME = System.currentTimeMillis();

	@Autowired
	private EncodedTextDocumentValidator validator;

	@RequestMapping("/faust-tei.rng")
	public void schema(WebRequest request, HttpServletResponse response) throws Exception {
		if (request.checkNotModified(CONTROLLER_CREATION_TIME)) {
			return;
		}

		response.setContentType(MediaType.APPLICATION_XML.toString());
		InputStream in = null;
		OutputStream out = response.getOutputStream();
		try {
			IOUtils.copy(in = validator.getSchemaUrl().openStream(), out);
			out.flush();
		} finally {
			in.close();
		}
	}
}
