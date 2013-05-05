/*
 * #%L
 * Text Resource Combo Utilities
 * %%
 * Copyright (C) 2012 Gregor Middell
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.faustedition.resource;

import com.google.common.collect.Iterables;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

/**
 * An ordered sequential combination of text-based resources.
 * <p/>
 * Serves as a container for resources, exposes aggregated metadata and is usable as an input supplier of the joint
 * contents in the contained resources.
 *
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextResourceCombo extends ArrayList<TextResource> implements InputSupplier<Reader> {

	/**
	 * Constructor.
	 *
	 * @param resources the resources to be contained in this combo. The iteration order of the parameter is
	 *                  supposed to be significant.
	 */
	public TextResourceCombo(Iterable<TextResource> resources) {
		super(Iterables.size(resources));
		Iterables.addAll(this, resources);
	}

	/**
	 * Determines the media type of this combo by retrieving it from the first member.
	 *
	 * @return the media type or <code>text/plain</code> in the case of an empty combo
	 */
	public String getMediaType() {
		return (isEmpty() ? TextResource.TEXT_PLAIN : get(0).getMediaType());
	}

	/**
	 * Determines the latest modification time of all contained resources.
	 *
	 * @return the latest modification time
	 * @see java.io.File#lastModified()
	 */
	public long lastModified() {
		long lastModified = 0;
		for (TextResource rd : this) {
			lastModified = Math.max(lastModified, rd.lastModified);
		}
		return (lastModified > 0 ? lastModified : System.currentTimeMillis());
	}

	/**
	 * Determines the smallest maximum cache age of all contained resources.
	 *
	 * @return the maximum cache age of this combo (in seconds)
	 */
	public long maxAge() {
		long maxAge = Long.MAX_VALUE;
		for (TextResource rd : this) {
			maxAge = Math.min(maxAge, rd.maxAge);
		}
		return (maxAge < Long.MAX_VALUE ? maxAge : 0);
	}

	/**
	 * Provides a joint reader of all contained resources in their given order.
	 *
	 * @return a reader concatenating the contents of all resources in the combo
	 * @throws IOException
	 */
	@Override
	public Reader getInput() throws IOException {
		return CharStreams.join(this).getInput();
	}
}
