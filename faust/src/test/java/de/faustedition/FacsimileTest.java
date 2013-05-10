package de.faustedition;

import com.google.inject.Guice;
import de.faustedition.facsimile.InternetImageServer;
import org.junit.Test;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class FacsimileTest {

    @Test
    public void imageInfos() {
        final InternetImageServer imageServer = Guice.createInjector(
                new ConfigurationModule(),
                new HttpModule()
        ).getInstance(InternetImageServer.class);

        System.out.println(imageServer.dimensionOf(new FaustURI(FaustAuthority.FACSIMILE, "/ub_leipzig/Slg_Hirzel_b492/a")));
    }
}
