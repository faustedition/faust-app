package de.faustedition.facsimile;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.UriBuilder;
import java.awt.Dimension;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class InternetImageServer {

    public static final URI BASE_URI = URI.create("https://faustedition.uni-wuerzburg.de/images/iipsrv.fcgi");

    private static final Logger LOG = Logger.getLogger(InternetImageServer.class.getName());

    private static final Splitter LINE_SPLITTER = Splitter.on(Pattern.compile("[\n\r]+")).omitEmptyStrings().trimResults();

    private static final Pattern IMAGE_DIMENSION_PATTERN = Pattern.compile("([0-9]+)\\s+([0-9]+)");

    private final Cache<FaustURI, Map<String, String>> imageInfoCache = CacheBuilder.newBuilder().build();

    @Inject
    public InternetImageServer() {
    }

    public Map<String, String> imageInfo(final FaustURI facsimile) {
        try {
            return imageInfoCache.get(facsimile, new Callable<Map<String, String>>() {
                @Override
                public Map<String, String> call() throws Exception {
                    final URI uri = uriFor(facsimile).queryParam("obj", "IIP,1.0")
                            .queryParam("obj", "Max-size")
                            .queryParam("obj", "Tile-size")
                            .queryParam("obj", "Resolution-number")
                            .build();

                    final String imageInfoBody = ClientBuilder.newClient().target(uri).request().get(String.class);

                    final Map<String, String> imageInfo = Maps.newHashMap();
                    for (String line : LINE_SPLITTER.split(imageInfoBody)) {
                        final int separatorIdx = line.indexOf(':');
                        if (separatorIdx <= 0 || separatorIdx >= (line.length() - 1)) {
                            throw new IllegalStateException(line);
                        }
                        imageInfo.put(line.substring(0, separatorIdx), line.substring(separatorIdx + 1));
                    }

                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, Joiner.on(" => ").join(facsimile, imageInfo));
                    }

                    for (Map.Entry<String, String> info : imageInfo.entrySet()) {
                        if (info.getKey().startsWith("Error")) {
                            throw new IllegalArgumentException(facsimile.toString());
                        }
                    }

                    return imageInfo;
                }
            });
        } catch (ExecutionException e) {
            throw Throwables.propagate(Throwables.getRootCause(e));
        }
    }

    public UriBuilder uriFor(FaustURI facsimile) {
        Preconditions.checkArgument(facsimile.getAuthority() == FaustAuthority.FACSIMILE);
        final String imagePath = facsimile.getPath().replaceAll("^/+", "") + ".tif";
        return UriBuilder.fromUri(BASE_URI).queryParam("FIF", imagePath);
    }

    public Dimension dimensionOf(FaustURI facsimile) {
        final String dimensionDesc = Objects.firstNonNull(imageInfo(facsimile).get("Max-size"), "0 0");
        final Matcher dimensionMatcher = IMAGE_DIMENSION_PATTERN.matcher(dimensionDesc);
        Preconditions.checkState(dimensionMatcher.matches(), facsimile + ": " + dimensionDesc);
        return new Dimension(Integer.parseInt(dimensionMatcher.group(1)), Integer.parseInt(dimensionMatcher.group(2)));
    }

}
