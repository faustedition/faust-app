package de.faustedition.xml;

import javax.xml.namespace.NamespaceContext;

import org.junit.Assert;
import org.junit.Test;


public class NamespacesTest {
    private NamespaceContext nsCtx = Namespaces.INSTANCE;

    @Test
    public void retrieveNamespacesByPrefix() {
        Assert.assertEquals(Namespaces.FAUST_NS_URI, nsCtx.getNamespaceURI("f"));
        Assert.assertEquals(Namespaces.TEI_NS_URI, nsCtx.getNamespaceURI("tei"));
    }
}
