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

package de.faustedition.facsimile;

import java.io.File;
import java.io.IOException;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component
public class FacsimileFinder extends Finder implements InitializingBean {

	private static final String FACSIMILE_TILE_CACHE = "facsimileTiles";
	private static final String FACSIMILE_METADATA_CACHE = "facsimileMetadata";

	@Autowired
	private Environment environment;

	@Autowired
	private CacheManager cacheManager;

	private File home;
	private String fileExtension;
	private File defaultFacsimile;
	private Ehcache tileCache;
	private Ehcache metadataCache;

	@Override
	public ServerResource find(Request request, Response response) {
		try {
			final File facsimile = findFacsimile(request.getResourceRef().getRelativeRef().getPath());
			final Form query = request.getResourceRef().getQueryAsForm();

			if (query.getFirst("metadata") != null) {
				return new FacsimileMetadataResource(metadataCache, new FacsimileTile(facsimile));
			} else {
				return new FacsimileTileResource(tileCache, new FacsimileTile(
					facsimile,
					Math.max(0, Integer.parseInt(query.getFirstValue("zoom", "0"))),
					Math.max(0, Integer.parseInt(query.getFirstValue("x", "0"))),
					Math.max(0, Integer.parseInt(query.getFirstValue("y", "0")))));
			}
		} catch (IOException e) {
			throw Throwables.propagate(e);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public File findFacsimile(String path) throws IOException, IllegalArgumentException {
		final File facsimile = new File(home, path.replaceAll("^/+", "") + "." + fileExtension);
		Preconditions.checkArgument(!facsimile.exists() || (facsimile.isFile() && isInHome(facsimile)), path);
		return (facsimile.exists() ? facsimile : defaultFacsimile);
	}

	private boolean isInHome(File file) {
		File parent = file.getParentFile();
		while (parent != null) {
			if (parent.equals(home)) {
				return true;
			}
			parent = parent.getParentFile();
		}
		return false;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.home = environment.getRequiredProperty("facsimile.home", File.class);
		this.fileExtension = environment.getRequiredProperty("facsimile.extension", String.class);
		this.defaultFacsimile = environment.getProperty("facsimile.default", File.class);

		Assert.isTrue(home.isDirectory(), home + " is not a directory");
		Assert.isTrue(defaultFacsimile == null || defaultFacsimile.isFile(), defaultFacsimile + " is not a file");

		this.tileCache = cacheManager.addCacheIfAbsent(FACSIMILE_TILE_CACHE);
		this.metadataCache = cacheManager.addCacheIfAbsent(FACSIMILE_METADATA_CACHE);
	}

}
