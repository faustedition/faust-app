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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.util.Map;

/**
 * Maps paths, specified e.g. in a URL, to text-based resources.
 * <p/>
 * The mapping can be configured by {@link #mount(String, TextResourceCollection) mounting}
 * a set of {@link TextResourceCollection resource collections} which are searched for resources matching the provided paths.
 *
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextResourceResolver implements Function<String, TextResource> {

    /**
     * Maps an ordered sequence of paths to a combination of text-based resources.
     *
     * @param paths the list of paths to be mapped
     * @return a new resource combo containing the mapped resources
     * @throws IllegalArgumentException in case a path cannot be mapped, e.g. because a specified path cannot be
     *                                  dereferenced
     */
    public TextResourceCombo resolve(Iterable<String> paths) throws IllegalArgumentException {
        return new TextResourceCombo(Iterables.transform(paths, this));
    }

    /**
     * Maps a path to a text-based resource.
     *
     * @param input the path to be mapped
     * @return the mapped resource
     * @throws IllegalArgumentException in case the path cannot be mapped, e.g. because the specified path cannot be
     *                                  dereferenced
     */
    @Override
    public TextResource apply(String input) throws IllegalArgumentException {
        for (Map.Entry<String, TextResourceCollection> mountPoint : mountPoints.entrySet()) {
            String rootPath = mountPoint.getKey();
            if (input.startsWith(rootPath)) {
                try {
                    return mountPoint.getValue().resolve(input.substring(rootPath.length()).replaceAll("^\\/+", ""));
                } catch (IOException e) {
                }
            }
        }
        throw new IllegalArgumentException(input);

    }

    /**
     * Registers a collection with the resolver, whose contents it can subsequently resolve.
     *
     * @param root       the path prefix under which resources of this mount point will be made available
     * @param collection the collection to be registered
     */
    public void mount(String root, TextResourceCollection collection) {
        mountPoints.put(root, collection);
    }

    private final Map<String, TextResourceCollection> mountPoints = Maps.newHashMap();

}
