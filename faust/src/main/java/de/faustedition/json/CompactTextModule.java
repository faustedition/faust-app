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
