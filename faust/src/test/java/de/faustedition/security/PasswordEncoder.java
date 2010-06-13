package de.faustedition.security;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import de.faustedition.Log;

public class PasswordEncoder {
	public static final String[] PASSWORDS = new String[] { "hallo", "welt" };

	@Test
	public void encode() {
		for (String password : PASSWORDS) {
			Log.LOGGER.info(String.format("%s ==> %s", password, DigestUtils.shaHex(password)));
		}
	}
}
