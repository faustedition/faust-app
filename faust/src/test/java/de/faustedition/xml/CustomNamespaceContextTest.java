package de.faustedition.xml;

import javax.xml.namespace.NamespaceContext;

import org.junit.Assert;
import org.junit.Test;

import de.faustedition.tei.EncodedTextDocument;

public class CustomNamespaceContextTest {
    private NamespaceContext nsCtx = CustomNamespaceContext.INSTANCE;

    @Test
    public void retrieveNamespacesByPrefix() {
        Assert.assertEquals(XmlDocument.FAUST_NS_URI, nsCtx.getNamespaceURI("f"));
        Assert.assertEquals(EncodedTextDocument.TEI_NS_URI, nsCtx.getNamespaceURI("tei"));
    }
}
