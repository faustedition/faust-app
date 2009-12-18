// Copyright 2008-2009 severally by the contributors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package net.sf.practicalxml.converter.bean;

import java.util.HashMap;
import java.util.Map;


/**
 *  A thread-safe cache of {@link Introspection} objects. May be constructed
 *  using either a local or shared (static) cache.
 */
public class IntrospectionCache
{
    private static Map<Class<?>,Introspection> _staticCache = new HashMap<Class<?>,Introspection>();
    private Map<Class<?>,Introspection> _cache;


    /**
     *  Creates an instance that uses a local cache.
     */
    public IntrospectionCache()
    {
        this(false);
    }


    /**
     *  Creates an instance that will either use a local or shared (static) cache.
     */
    public IntrospectionCache(boolean shared)
    {
        _cache = shared ? _staticCache
                        : new HashMap<Class<?>,Introspection>();
    }


    /**
     *  Returns an {@link Introspection} of the passed class.
     *
     *  @throws ConversionError if unable to introspect the class.
     */
    public synchronized Introspection lookup(Class<?> klass)
    {
        Introspection result = _cache.get(klass);
        if (result == null)
        {
            result = new Introspection(klass);
            _cache.put(klass, result);
        }
        return result;
    }
}
