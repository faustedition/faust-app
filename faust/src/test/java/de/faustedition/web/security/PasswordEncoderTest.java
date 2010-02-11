package de.faustedition.web.security;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import de.faustedition.util.LoggingUtil;

public class PasswordEncoderTest {
	public static final String[] PASSWORDS = new String[] { "hallo", "welt" };

	@Test
	public void encode() {
		for (String password : PASSWORDS) {
			LoggingUtil.LOG.info(String.format("%s ==> %s", password, DigestUtils.shaHex(password)));
		}
	}
}
