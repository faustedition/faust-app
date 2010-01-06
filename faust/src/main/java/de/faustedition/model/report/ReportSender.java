package de.faustedition.model.report;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import de.faustedition.util.ErrorUtil;
import de.faustedition.util.LoggingUtil;

public class ReportSender {
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
			return;
		}
		
		String subject = report.getSubject();
		String body = report.getBody();

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
			LoggingUtil.LOG.debug("Error while sending mail report (mail contents follow exception output)", e);
			LoggingUtil.LOG.warn(String.format("Subject: %s\n\n%s", subject, body));
		} catch (MessagingException e) {
			throw ErrorUtil.fatal(e);
		}
	}

}
