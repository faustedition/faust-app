/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ettrema.console;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

import static org.easymock.EasyMock.*;

/**
 *
 * @author brad
 */
public class CursorTest extends TestCase {

    private ResourceFactory resourceFactory;
    private CollectionResource col;
    private Resource child;
    private String host;

    @Override
    protected void setUp() throws Exception {
        resourceFactory = createMock( ResourceFactory.class );
        col = createMock( CollectionResource.class );
        child = createMock( Resource.class );
        host = "test.host";
    }

    public void testFindChild() {
        Cursor cursor = new Cursor( resourceFactory, host, "/abc" );
        expect( resourceFactory.getResource( host, "/abc" ) ).andReturn( col );
        expect( col.child( "x" ) ).andReturn( child );
        replay( resourceFactory, col );
        Cursor c2 = cursor.find( "x" );
        assertSame( c2.getResource(), child );
        verify( resourceFactory, col );
        assertEquals( "/abc/x", c2.getPath().toString() );
    }

    public void testFindAbsolute() {
        Cursor cursor = new Cursor( resourceFactory, host, "/abc" );
        expect( resourceFactory.getResource( host, "/" ) ).andReturn( col );
        expect( col.child( "x" ) ).andReturn( child );
        replay( resourceFactory, col );
        Cursor c2 = cursor.find( "/x" );
        assertSame( c2.getResource(), child );
        verify( resourceFactory, col );
        assertEquals( "/x", c2.getPath().toString() );
    }

    public void testFindSame() {
        Cursor cursor = new Cursor( resourceFactory, host, "/abc" );
        expect( resourceFactory.getResource( host, "/abc" ) ).andReturn( col );
        replay( resourceFactory );
        Cursor c2 = cursor.find( "." );
        assertSame( c2.getResource(), col );
        verify( resourceFactory );
        assertEquals( "/abc", c2.getPath().toString() );
    }

    public void testFindParent() {
        Cursor cursor = new Cursor( resourceFactory, host, "/abc/x" );
        expect( resourceFactory.getResource( host, "/abc/x" ) ).andReturn( child );
        expect( resourceFactory.getResource( host, "/abc" ) ).andReturn( col );
        replay( resourceFactory );
        Cursor c2 = cursor.find( ".." );
        assertSame( c2.getResource(), col );
        verify( resourceFactory );
        assertEquals( "/abc", c2.getPath().toString() );
    }

    public void testFindSequential() {
        Cursor cursor = new Cursor( resourceFactory, host, "/abc" );
        expect( resourceFactory.getResource( host, "/abc" ) ).andReturn( col ).times( 2 );

        expect( col.child( "x" ) ).andReturn( child );
        expect( col.child( "a" ) ).andReturn( child );

        replay( resourceFactory, col );
        Cursor c2 = cursor.find( "x/../a" );
        assertSame( c2.getResource(), child );
        verify( resourceFactory );
        assertEquals( "/abc/a", c2.getPath().toString() );
    }

    public void testFindAllWithRegex() {
        Cursor cursor = new Cursor( resourceFactory, host, "/abc" );
        List allChildren = childList();
        expect( resourceFactory.getResource( host, "/abc" ) ).andReturn( col );
        expect(col.getChildren()).andReturn(allChildren);
        replay(resourceFactory, col);
        List<Resource> list = cursor.childrenWithFilter( ".*" );
        System.out.println( "msg: " + cursor.getMessage() );
        assertNotNull( list );
        assertEquals( 3, list.size());
    }

    public void testFindOneWithRegex() {
        Cursor cursor = new Cursor( resourceFactory, host, "/abc" );
        List allChildren = childList();
        expect( resourceFactory.getResource( host, "/abc" ) ).andReturn( col );
        expect(col.getChildren()).andReturn(allChildren);
        replay(resourceFactory, col);
        List<Resource> list = cursor.childrenWithFilter( "a1" );
        System.out.println( "msg: " + cursor.getMessage() );
        assertNotNull( list );
        assertEquals( 1, list.size());
    }

    private List childList() {
        List allChildren = new ArrayList<Resource>();
        allChildren.add( createResource( "a1" ) );
        allChildren.add( createResource( "a2" ) );
        allChildren.add( createResource( "a3" ) );
        return allChildren;
    }

    private Resource createResource(String name) {
        Resource r = createMock( Resource.class );
        expect(r.getName()).andReturn( name ).anyTimes();
        replay(r);
        return r;
    }
}
