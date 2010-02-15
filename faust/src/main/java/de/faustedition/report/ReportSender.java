package de.faustedition.report;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import de.faustedition.ErrorUtil;

public class ReportSender {
	private static final String SUBJECT_FORMAT = "[Faust-Edition-Report] {0}";
	private static final Logger LOG = LoggerFactory.getLogger(ReportSender.class);
	@Autowired
	private JavaMailSender mailSender;

	private final String from;
	private final String to;
	private final String[] cc;

	public ReportSender(String from, String to) {
		this(from, to, null);
	}

	public ReportSender(String from, String to, String[] cc) {
		this.from = from;
		this.to = to;
		this.cc = cc;
	}

	public void send(Report report) {
		if (report.isEmpty()) {
			LOG.debug("Not sending report {}; it is empty.", report);
			return;
		}

		String subject = MessageFormat.format(SUBJECT_FORMAT, report.getSubject());
		StringWriter bodyWriter = new StringWriter();
		report.printBody(new PrintWriter(bodyWriter));
		String body = bodyWriter.toString();

		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message);
			helper.setFrom(from);
			helper.setTo(to);
			if (cc != null && cc.length > 0) {
				helper.setCc(cc);
			}
			helper.setSubject(subject);
			helper.setText(body);
			mailSender.send(message);
		} catch (MailException e) {
			LOG.debug("Error while sending mail report (mail contents follow exception output)", e);
			LOG.warn("Subject: " + subject + "\n\n" + body);
		} catch (MessagingException e) {
			throw ErrorUtil.fatal(e);
		}
	}

}
