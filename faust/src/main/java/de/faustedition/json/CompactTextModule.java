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

package de.faustedition.json;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.module.SimpleModule;

import eu.interedition.text.Name;
import eu.interedition.text.json.NameDeserializer;
import eu.interedition.text.json.NameSerializer;
import eu.interedition.text.json.QueryResultSerializer;
import eu.interedition.text.json.TextRangeSerializer;
import eu.interedition.text.json.TextStreamSerializer;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class CompactTextModule extends SimpleModule {

    public CompactTextModule() {
        super(CompactTextModule.class.getPackage().getName(), Version.unknownVersion());
        addSerializer(new NameSerializer());
        addSerializer(new TextRangeSerializer());
        addSerializer(new CompactAnchorSerializer());
        addSerializer(new CompactLayerSerializer());
        addSerializer(new QueryResultSerializer());
        addSerializer(new TextStreamSerializer());

        addDeserializer(Name.class, new NameDeserializer());
        //addDeserializer(TextRange.class, new RangeDeserializer());
    }

}
