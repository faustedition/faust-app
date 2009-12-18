package com.ettrema.json;

import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.PropertyWriter;
import com.ettrema.json.MapBuildingPropertyConsumer.Props;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;

import static org.easymock.EasyMock.*;

/**
 *
 * @author brad
 */
public class MapBuildingPropertyConsumerTest extends TestCase {

    MapBuildingPropertyConsumer consumer;
    Set<PropertyWriter> set;
    String href = "";

    public MapBuildingPropertyConsumerTest( String testName ) {
        super( testName );
    }

    @Override
    protected void setUp() throws Exception {
        consumer = new MapBuildingPropertyConsumer();
        set = new HashSet<PropertyWriter>();
    }

    public void testConsumeProperties() {
        PropFindableResource pfr = createMock( PropFindableResource.class );
        consumer.consumeProperties( set, set, href, pfr, 0 );

        pfr = createMock( PropFindableResource.class );
        consumer.consumeProperties( set, set, href, pfr, 1 );

        pfr = createMock( PropFindableResource.class );
        consumer.consumeProperties( set, set, href, pfr, 1 );

        pfr = createMock( PropFindableResource.class );
        consumer.consumeProperties( set, set, href, pfr, 2 );

        pfr = createMock( PropFindableResource.class );
        consumer.consumeProperties( set, set, href, pfr, 2 );

        pfr = createMock( PropFindableResource.class );
        consumer.consumeProperties( set, set, href, pfr, 1 );

        Map<String, Object> props = consumer.getProperties();
        assertNotNull( props );
        assertEquals( 1, props.size() );

        List<Map<String, Object>> children = (List<Map<String, Object>>) props.get( "children" );
        assertNotNull( children );
        assertEquals( 3, children.size() );

        Props childProps = (Props) children.get( 1 );
        assertNotNull( childProps);
        List<Map<String, Object>> children2 = (List<Map<String, Object>>) childProps.get( "children" );
        assertNotNull( children2);
        assertEquals( 2, children2.size() );
    }
}
