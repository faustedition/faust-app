package de.faustedition.db;

import java.io.File;
import java.util.Properties;

import org.hsqldb.cmdline.SqlTool;
import org.hsqldb.cmdline.SqlTool.SqlToolException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DbConsole {

	public static void main(String[] args) {
		try {
			Properties config = new ClassPathXmlApplicationContext("classpath:/applicationConfig.xml").getBean("config", Properties.class);
			SqlTool.objectMain(new String[] { "--inlineRc=url=" + "jdbc:hsqldb:file:" +//
					new File(config.getProperty("db.home")).getAbsolutePath() + "/faust" + ",user=SA,password=" });
		} catch (SqlToolException e) {
			e.printStackTrace();
		}
	}
}
