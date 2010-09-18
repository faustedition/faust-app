package de.faustedition.document;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import com.google.inject.Guice;
import com.google.inject.Injector;

import de.faustedition.graph.GraphReference;
import de.faustedition.inject.ConfigurationModule;
import de.faustedition.inject.DataAccessModule;

public class ArchiveTest {

    private Injector injector = Guice.createInjector(new ConfigurationModule(), new DataAccessModule());
    private GraphDatabaseService db;
    private Transaction tx;
    private boolean commitTransaction = false;

    @Before
    public void startTransaction() {
        db = injector.getInstance(GraphReference.class).getGraphDatabaseService();
        tx = db.beginTx();
    }

    @After
    public void endTransaction() {
        if (tx != null) {
            if (commitTransaction) {
                tx.success();
            }
            tx.finish();
        }
    }

    @Test
    public void archiveAddition() {
        final ArchiveCollection archives = injector.getInstance(GraphReference.class).getArchives();
        Assert.assertNotNull(archives);        
        Archive a1 = new Archive(db.createNode(), "test1");
        Archive a2 = new Archive(db.createNode(), "test2");
        
        archives.add(a1);
        Assert.assertTrue(archives.contains(a1));
        
        archives.add(a2);
        Assert.assertTrue(archives.contains(a2));
    }
}
