/*
 * Copyright (c) 2014 Faust Edition development team.
 *
 * This file is part of the Faust Edition.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.faustedition;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;

@Component
public class EmailReporter implements InitializingBean {

	@Autowired
	private Logger logger;

	@Autowired
	private Environment environment;

	private boolean mailEnabled;

	@Override
	public void afterPropertiesSet() throws Exception {
		this.mailEnabled = environment.getRequiredProperty("email.enabled", Boolean.class);
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
			logger.warn(String.format("\n\nSubject: %s\n\n%s\n\n", subject, body));
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
