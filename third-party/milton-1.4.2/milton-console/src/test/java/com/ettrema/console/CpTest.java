/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ettrema.console;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.FolderResource;
import com.bradmcevoy.http.ResourceFactory;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

import static org.easymock.EasyMock.*;

/**
 *
 * @author brad
 */
public class CpTest extends TestCase {

    Cp cp;
    ConsoleResourceFactory consoleResourceFactory;
    ResourceFactory resourceFactory;
    String host = "aHost";
    List<String> args;
    CollectionResource col;
    CopyableResource sourceFile;
    FolderResource sourceFolder;
    FolderResource destFolder;
    List<CopyableResource> sourceChilren;

    @Override
    protected void setUp() throws Exception {
        resourceFactory = createMock( ResourceFactory.class );
        consoleResourceFactory = new ConsoleResourceFactory( resourceFactory, "/console", "/", new ArrayList<ConsoleCommandFactory>(), null);
        args = new ArrayList<String>();
        col = createMock( CollectionResource.class );
        sourceFile = createMock( CopyableResource.class );
        sourceFolder = createMock( FolderResource.class );
        destFolder = createMock( FolderResource.class );
        sourceChilren = new ArrayList<CopyableResource>();
        for( int i = 0; i < 3; i++ ) {
            CopyableResource cr = createMock( CopyableResource.class );
            String name = "x" + i;
            System.out.println( "adding: " + name );
            expect( cr.getName() ).andReturn( name ).anyTimes();
            sourceChilren.add( cr );
        }
    }

    public void testCopyFileNoDest() {
        expect( resourceFactory.getResource( host, "" ) ).andReturn( col ).anyTimes();
        expect( col.child( "a" ) ).andReturn( sourceFile );
        expect( col.child( "b" ) ).andReturn( null );
        expect( col.getName() ).andReturn( "" );
        expect( sourceFile.getName() ).andReturn( "a" );
        sourceFile.copyTo( col, "b" );
        expectLastCall();
        replay( resourceFactory, col, sourceFile );

        args.add( "a" );
        args.add( "b" );
        cp = new Cp( args, host, "/", consoleResourceFactory );
        cp.execute();
    }

    public void testCopyFolderNoDest() {
        expect( resourceFactory.getResource( host, "" ) ).andReturn( col ).anyTimes();
        expect( col.child( "a" ) ).andReturn( sourceFolder );
        expect( col.child( "b" ) ).andReturn( null );
        expect( col.getName() ).andReturn( "" );
        expect( sourceFolder.getName() ).andReturn( "a" );
        sourceFolder.copyTo( col, "b" );
        expectLastCall();
        replay( resourceFactory, col, sourceFolder );

        args.add( "a" );
        args.add( "b" );
        cp = new Cp( args, host, "/", consoleResourceFactory );
        cp.execute();
    }

    public void testCopyFolderDestExistsAndIsFolder() {
        expect( resourceFactory.getResource( host, "" ) ).andReturn( col );
        expect( col.child( "a" ) ).andReturn( sourceFolder );
        expect( col.child( "b" ) ).andReturn( destFolder );
        expect( sourceFolder.getName() ).andReturn( "a" );
        expect( sourceFolder.getChildren() ).andReturn( (List) sourceChilren );
        expect( destFolder.getName() ).andReturn( "b" ).anyTimes();
        replay( resourceFactory, col, sourceFolder, destFolder );
        replayChildrenWithCopyTo( destFolder );

        args.add( "a" );
        args.add( "b" );
        cp = new Cp( args, host, "/", consoleResourceFactory );
        cp.execute();
    }

    public void testCopyFolderWithRegex_DestExistsAndIsFolder() {
        expect( resourceFactory.getResource( host, "" ) ).andReturn( col );
        expect( resourceFactory.getResource( host, "/a" ) ).andReturn( sourceFolder );
        expect( col.child( "a" ) ).andReturn( sourceFolder );
        expect( col.child( "b" ) ).andReturn( destFolder );
        expect( sourceFolder.child( "x[12]" ) ).andReturn( null );
        expect( sourceFolder.getChildren() ).andReturn( (List) sourceChilren );
        expect( destFolder.getName() ).andReturn( "b" ).anyTimes();
        replay( resourceFactory, col, sourceFolder, destFolder );
        sourceChilren.get( 1 ).copyTo( destFolder, "x1" );
        expectLastCall();
        sourceChilren.get( 2 ).copyTo( destFolder, "x2" );
        expectLastCall();
        replayChildren();

        args.add( "a/x[12]" ); // on copy x1 and x2
        args.add( "b" );
        cp = new Cp( args, host, "/", consoleResourceFactory );
        cp.execute();
        verify( sourceFolder, destFolder, resourceFactory, col );
    }

    private void replayChildren() {
        for( CopyableResource cr : sourceChilren ) {
            replay( cr );
        }
    }

    private void replayChildrenWithCopyTo( FolderResource destFolder ) {
        int i = 0;
        for( CopyableResource cr : sourceChilren ) {
            cr.copyTo( destFolder, "x" + i++ );
            expectLastCall();
        }
        replayChildren();
    }
}
