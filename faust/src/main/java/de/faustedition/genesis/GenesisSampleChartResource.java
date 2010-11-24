package de.faustedition.genesis;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.inject.Inject;

public class GenesisSampleChartResource extends ServerResource {

	private final GenesisSampleChart chart;

	@Inject
	public GenesisSampleChartResource(GenesisSampleChart chart) {
		this.chart = chart;
	}

	@Get("png")
	public Representation generate() {
		return new OutputRepresentation(MediaType.IMAGE_PNG) {

			@Override
			public void write(OutputStream outputStream) throws IOException {
				chart.render(outputStream, new PrintWriter(new StringWriter()), "genesisChart");
			}
		};
	}
}
