package de.faustedition.report;

import java.text.MessageFormat;
import java.util.Locale;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.faustedition.Log;

@Service
public class ReportManager {
	private static final String SUBJECT_FORMAT = "[Faust-Edition-Report] {0}";
	private static final Locale REPORT_LOCALE = Locale.GERMAN;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private SimpleJdbcTemplate jt;

	@Autowired
	private MessageSource messageSource;

	private String from = "Faust-Edition <noreply@faustedition.net>";
	private String to = "Gregor Middell <gregor@middell.net>";
	private String[] cc;

	@Transactional
	public void send(Report report) {
		report.save(jt);
		if (report.isEmpty()) {
			Log.LOGGER.debug("Not sending report '{}'; it is empty.", report);
			return;
		}

		String subject = MessageFormat.format(SUBJECT_FORMAT,//
				messageSource.getMessage("report." + report.getName(), null, REPORT_LOCALE));

		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message);
			helper.setFrom(from);
			helper.setTo(to);
			if (cc != null && cc.length > 0) {
				helper.setCc(cc);
			}
			helper.setSubject(subject);
			helper.setText(report.getBody());
			mailSender.send(message);
		} catch (MailException e) {
			Log.LOGGER.debug("Error while sending mail report (mail contents follow exception output)", e);
			Log.LOGGER.warn("Subject: " + subject + "\n\n" + report.getBody());
		} catch (MessagingException e) {
			throw Log.fatalError(e);
		}
	}

}
