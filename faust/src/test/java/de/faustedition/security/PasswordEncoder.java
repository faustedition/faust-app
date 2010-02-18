package de.faustedition.security;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordEncoder {
	private static final Logger LOG = LoggerFactory.getLogger(PasswordEncoder.class);
	
	public static final String[] PASSWORDS = new String[] { "hallo", "welt" };

	@Test
	public void encode() {
		for (String password : PASSWORDS) {
			LOG.info(String.format("%s ==> %s", password, DigestUtils.shaHex(password)));
		}
	}
}
