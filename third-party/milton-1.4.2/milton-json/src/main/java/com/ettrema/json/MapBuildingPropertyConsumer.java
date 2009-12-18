package com.ettrema.json;

import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.PropertyConsumer;
import com.bradmcevoy.http.PropertyWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class MapBuildingPropertyConsumer implements PropertyConsumer{

    private static final Logger log = LoggerFactory.getLogger(MapBuildingPropertyConsumer.class);

    Props properties = new Props(null,0);

    Props lastProps;

    public void consumeProperties( Set<PropertyWriter> knownProperties, Set<PropertyWriter> unknownProperties, String href, PropFindableResource resource, int depth ) {
        log.debug( "consumeProperties");
        if( lastProps == null ) {
            lastProps = properties;
        } else if( depth > lastProps.depth) {
            lastProps = new Props( lastProps, depth);
        } else if( depth < lastProps.depth ) {
            lastProps = lastProps.parent; // go up a level
            lastProps = new Props( lastProps.parent, depth);
        } else {
            lastProps = new Props( lastProps.parent, depth); // another resource at same level, add to same parent
        }
        addProps(knownProperties, resource, href, lastProps);

    }

    private void addProps( Set<PropertyWriter> knownProperties, PropFindableResource resource, String href, Map<String, Object> properties ) {
        for( PropertyWriter pw : knownProperties) {
            String key = pw.fieldName();
            Object value = pw.getValue( resource,href );
            properties.put( key, value );
        }
    }

    public Map<String, Object> getProperties() {
        return properties;
    }


    class Props extends HashMap<String, Object> {
        final Props parent;
        final int depth;

        public Props( Props parent,int depth ) {
            this.parent = parent;
            this.depth = depth;

            if( parent != null){
                List<Map<String,Object>> list = (List<Map<String, Object>>) parent.get( "children");
                if( list == null ) {
                    list = new ArrayList<Map<String, Object>>();
                    parent.put( "children", list);
                }
                list.add( this );
            }
        }

        
    }
}
