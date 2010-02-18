package de.faustedition;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/de/faustedition/tei/context.xml", "/de/faustedition/xml/context.xml",
		"/de/faustedition/db/context.xml", "/de/faustedition/facsimile/context.xml",
		"/de/faustedition/metadata/context.xml", "/de/faustedition/report/context.xml" })
public abstract class AbstractModelContextTest {

}
