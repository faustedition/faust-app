package de.faustedition.xml;

import javax.xml.namespace.NamespaceContext;

import org.junit.Assert;
import org.junit.Test;


public class CustomNamespaceContextTest {
    private NamespaceContext nsCtx = CustomNamespaceContext.INSTANCE;

    @Test
    public void retrieveNamespacesByPrefix() {
        Assert.assertEquals(Namespaces.FAUST_NS_URI, nsCtx.getNamespaceURI("f"));
        Assert.assertEquals(Namespaces.TEI_NS_URI, nsCtx.getNamespaceURI("tei"));
    }
}
