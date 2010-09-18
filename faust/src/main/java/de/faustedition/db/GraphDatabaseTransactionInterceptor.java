package de.faustedition.db;

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
    public void setGraphDatabaseRoot(GraphDatabaseRoot dbRoot) {
        this.db = dbRoot.getGraphDatabaseService();
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Starting graph database transaction for " + invocation.getClass() + "#" + invocation.getMethod().getName());
        }
        Transaction tx = db.beginTx();
        try {
            Object returnValue = invocation.proceed();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Commiting graph database transaction after successful execution of" + invocation.getClass() + "#"
                        + invocation.getMethod().getName());
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
                    logger.fine("Commiting graph database transaction after successful exception throw of " + invocation.getClass()
                            + "#" + invocation.getMethod().getName());
                }
                tx.success();
            } else {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Rolling back graph database transaction after exception throw of " + invocation.getClass() + "#"
                            + invocation.getMethod().getName());
                }
                tx.failure();
            }
            throw e;
        } finally {
            tx.finish();
        }
    }

}
