package de.faustedition.facsimile;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
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

    private static final Splitter LINE_SPLITTER = Splitter.on(Pattern.compile("[\n\r]+")).omitEmptyStrings().trimResults();

    private static final Pattern IMAGE_DIMENSION_PATTERN = Pattern.compile("([0-9]+)\\s+([0-9]+)");

    private final URI uri;
    private final ClientConfig clientConfig;
    private final Logger logger;

    private final Cache<String, Map<String, String>> imageInfoCache = CacheBuilder.newBuilder().build();

    @Inject
    public InternetImageServer(@Named("facsimile.iip.url") String url, ClientConfig clientConfig, Logger logger) {
        this.uri = URI.create(url);
        this.clientConfig = clientConfig;
        this.logger = logger;
    }

    public Map<String, String> imageInfo(final FaustURI facsimile) {
        Preconditions.checkArgument(facsimile.getAuthority() == FaustAuthority.FACSIMILE);
        final String imagePath = facsimile.getPath().replaceAll("^/+", "") + ".tif";
        try {
            return imageInfoCache.get(imagePath, new Callable<Map<String, String>>() {
                @Override
                public Map<String, String> call() throws Exception {
                    final String imageInfoBody = Client.create(clientConfig).resource(uri)
                            .queryParam("FIF", imagePath)
                            .queryParam("obj", "IIP,1.0")
                            .queryParam("obj", "Max-size")
                            .queryParam("obj", "Tile-size")
                            .queryParam("obj", "Resolution-number")
                            .get(String.class);

                    final Map<String, String> imageInfo = Maps.newHashMap();
                    for (String line : LINE_SPLITTER.split(imageInfoBody)) {
                        final int separatorIdx = line.indexOf(':');
                        if (separatorIdx <= 0 || separatorIdx >= (line.length() - 1)) {
                            throw new IllegalStateException(line);
                        }
                        imageInfo.put(line.substring(0, separatorIdx), line.substring(separatorIdx + 1));
                    }

                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, Joiner.on(" => ").join(facsimile, imageInfo));
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

    public Dimension dimensionOf(FaustURI facsimile) {
        final String dimensionDesc = Objects.firstNonNull(imageInfo(facsimile).get("Max-size"), "0 0");
        final Matcher dimensionMatcher = IMAGE_DIMENSION_PATTERN.matcher(dimensionDesc);
        Preconditions.checkState(dimensionMatcher.matches(), facsimile + ": " + dimensionDesc);
        return new Dimension(Integer.parseInt(dimensionMatcher.group(1)), Integer.parseInt(dimensionMatcher.group(2)));
    }

}
