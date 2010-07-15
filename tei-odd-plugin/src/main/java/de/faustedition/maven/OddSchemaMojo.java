package de.faustedition.maven;

import java.io.File;
import java.net.URI;
import java.util.Collections;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.FeatureKeys;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltTransformer;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.FileUtils;

/**
 * Generates XML schemata from ODDs.
 * 
 * @goal schema
 * @phase generate-resources
 */
public class OddSchemaMojo extends AbstractMojo {
	/**
	 * Base URL of the TEI P5 stylesheets.
	 * 
	 * @parameter expression="${teiStylesheetsUrl}"
	 *            default-value="http://www.tei-c.org/release/xml/tei/stylesheet/"
	 * @required
	 */
	private String stylesheetsUrl;

	/**
	 * URL to the P5 subset XML document.
	 * 
	 * @parameter expression="${teiP5subsetUrl}" default-value=
	 *            "http://www.tei-c.org/release/xml/tei/odd/p5subset.xml"
	 * @required
	 */
	private String p5subsetUrl;

	/**
	 * Source directory with ODD files.
	 * 
	 * @parameter expression="${teiSchemaSource}"
	 *            default-value="${basedir}/src/main/odd"
	 * @required
	 */
	private File source;

	/**
	 * Output directory for generated schemata.
	 * 
	 * @parameter expression=
	 *            "${project.build.directory}/generated-resources/tei/schema"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * The project.
	 * 
	 * @parameter expression="${project}
	 * @readonly
	 * @required
	 */
	private MavenProject project;

	/**
	 * @component
	 */
	private MavenProjectHelper projectHelper;

	public void execute() throws MojoExecutionException {
		try {
			if (!source.exists()) {
				getLog().debug(String.format("No ODD found: %s", source.getAbsolutePath()));
				return;
			}

			if (source.isDirectory()) {
				for (File file : source.listFiles()) {
					if (file.isDirectory()) {
						continue;
					}
					generateSchema(file);
				}
			} else {
				generateSchema(source);
			}

			if (outputDirectory.exists()) {
				projectHelper.addResource(project, outputDirectory.getAbsolutePath(), Collections.singletonList("*/**"), Collections.emptyList());
			}
		} catch (Exception e) {
			throw new MojoExecutionException("Error while generating schema from ODD");
		}
	}

	private void generateSchema(File schemaFile) throws Exception {
		final File out = new File(getOutputDirectory(), FileUtils.basename(schemaFile.getName()) + "rng");
		
		if (out.exists() && out.lastModified() >= schemaFile.lastModified()) {
			getLog().info(String.format("Schema '%s' exists and seems up-to-date. Skipping ODD evaluation", out.getAbsolutePath()));
			return;
		}
		getLog().info(String.format("Generating schema from %s into %s", schemaFile.getAbsolutePath(), out.getAbsolutePath()));
		
		

		Serializer serializer = new Serializer();
		serializer.setOutputFile(out);

		try {
			Processor processor = new Processor(false);
			processor.setConfigurationProperty(FeatureKeys.XINCLUDE, true);
			XsltCompiler xsltCompiler = processor.newXsltCompiler();

			XsltTransformer odd2relax = xsltCompiler.compile(new StreamSource(new URI(stylesheetsUrl).resolve("odds2/odd2relax.xsl").toASCIIString())).load();
			odd2relax.setDestination(serializer);
			odd2relax.setParameter(new QName("verbose"), new XdmAtomicValue("false"));
			odd2relax.setParameter(new QName("TEIC"), new XdmAtomicValue("true"));
			odd2relax.setParameter(new QName("lang"), new XdmAtomicValue("en"));
			odd2relax.setParameter(new QName("doclang"), new XdmAtomicValue("en"));
			odd2relax.setParameter(new QName("parameterize"), new XdmAtomicValue("false"));
			odd2relax.setParameter(new QName("patternPrefix"), new XdmAtomicValue("_tei"));

			XsltTransformer odd2odd = xsltCompiler.compile(new StreamSource(new URI(stylesheetsUrl).resolve("odds2/odd2odd.xsl").toASCIIString())).load();

			odd2odd.setParameter(new QName("selectedSchema"), new XdmAtomicValue("faust-tei"));
			odd2odd.setParameter(new QName("verbose"), new XdmAtomicValue("false"));
			odd2odd.setParameter(new QName("stripped"), new XdmAtomicValue("true"));
			odd2odd.setParameter(new QName("defaultSource"), new XdmAtomicValue(new URI(p5subsetUrl)));
			odd2odd.setParameter(new QName("TEIC"), new XdmAtomicValue("true"));
			odd2odd.setParameter(new QName("lang"), new XdmAtomicValue("en"));
			odd2odd.setParameter(new QName("doclang"), new XdmAtomicValue("en"));
			odd2odd.setParameter(new QName("useVersionFromTEI"), new XdmAtomicValue("true"));

			odd2odd.setSource(new StreamSource(schemaFile));
			odd2odd.setDestination(odd2relax);
			odd2odd.transform();
		} finally {
			serializer.close();
		}
	}

	private File getOutputDirectory() {
		if (!outputDirectory.exists()) {
			getLog().debug(String.format("Creating output directory %s", outputDirectory.getAbsolutePath()));
			outputDirectory.mkdirs();
		}
		return outputDirectory;
	}
}
