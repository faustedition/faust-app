package de.faustedition;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

@Singleton
public class EmailReporter {

	private final boolean mailEnabled;
    private final Logger logger;

    @Inject
    public EmailReporter(@Named("email.enabled") boolean mailEnabled, Logger logger) {
        this.mailEnabled = mailEnabled;
        this.logger = logger;
    }

	public void send(String subject, ReportCreator creator) throws EmailException {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(subject));
		
		final StringWriter bodyWriter = new StringWriter();
		final PrintWriter bodyPrintWriter = new PrintWriter(bodyWriter);
		
		creator.create(bodyPrintWriter);
		bodyPrintWriter.println();
		bodyPrintWriter.println("--");
		bodyPrintWriter.println("Digitale Faust-Edition");
		bodyPrintWriter.println("http://www.faustedition.net/");

		final String body = bodyWriter.toString();		
		if (!mailEnabled) {
			logger.warning(String.format("\n\nSubject: %s\n\n%s\n\n", subject, body));
			return;
		}
		
		final SimpleEmail email = new SimpleEmail();
		email.setHostName("localhost");
		email.setFrom("noreply@faustedition.net", "Digitale Faust-Edition");
		email.addTo("gregor@middell.net", "Gregor Middell");
		email.addTo("m.wissenbach@gmx.de", "Moritz Wissenbach");
		email.setSubject(subject);		
		email.setMsg(body);
		email.send();
	}
	
	public interface ReportCreator {
		void create(PrintWriter body);
	}
}
