package de.faustedition.model.report;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class ReportConfiguration {
	@Bean
	public JavaMailSender mailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setDefaultEncoding("UTF-8");
		mailSender.setHost("localhost");
		return mailSender;
	}

	@Bean
	public ReportSender reportSender() {
		return new ReportSender("Faust-Edition <noreply@faustedition.net>", "Gregor Middell <gregor@middell.net>");
	}
}
