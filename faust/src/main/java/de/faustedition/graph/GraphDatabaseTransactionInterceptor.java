package de.faustedition.graph;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import com.google.inject.Inject;

public class GraphDatabaseTransactionInterceptor implements MethodInterceptor {

    private GraphDatabaseService db;

    @Inject
    private Logger logger;

    @Inject
    public void setGraphDatabaseRoot(GraphReference graph) {
        this.db = graph.getGraphDatabaseService();
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Starting graph database transaction");
        }
        Transaction tx = db.beginTx();
        try {
            Object returnValue = invocation.proceed();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Commiting graph database transaction");
            }
            tx.success();
            return returnValue;
        } catch (Throwable e) {
            final GraphDatabaseTransactional txAnnotation = invocation.getMethod().getAnnotation(GraphDatabaseTransactional.class);
            boolean successfulException = false;
            final Class<? extends Throwable> thrownType = e.getClass();
            for (Class<? extends Throwable> successfulType : txAnnotation.successfulExceptions()) {
                if (successfulType.isAssignableFrom(thrownType)) {
                    successfulException = true;
                    break;
                }
            }
            if (successfulException) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Commiting graph database transaction");
                }
                tx.success();
            } else {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Rolling back graph database transaction after exception throw of " + e);
                }
                tx.failure();
            }
            throw e;
        } finally {
            tx.finish();
        }
    }
}
