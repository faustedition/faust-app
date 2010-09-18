package de.faustedition.xml;

import javax.xml.namespace.NamespaceContext;

import org.junit.Assert;
import org.junit.Test;

import de.faustedition.FaustURI;

public class CustomNamespaceContextTest {
    private NamespaceContext nsCtx = CustomNamespaceContext.INSTANCE;

    @Test
    public void retrieveNamespacesByPrefix() {
        Assert.assertEquals(FaustURI.FAUST_NS_URI, nsCtx.getNamespaceURI("f"));
        Assert.assertEquals(CustomNamespaceMap.TEI_NS_URI, nsCtx.getNamespaceURI("tei"));
    }
}
