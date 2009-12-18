// Copyright 2008-2009 severally by the contributors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package net.sf.practicalxml.builder;

import org.w3c.dom.Element;
import org.xml.sax.helpers.AttributesImpl;


/**
 *  Holds an attribute, which may or may not be namespaced.
 */
public class AttributeNode
extends Node
implements java.io.Serializable
{
    private static final long serialVersionUID = 2L;

    private String _nsUri;
    private String _qname;
    private String _lclName;
    private String _value;

    public AttributeNode(String nsUri, String qname, String value)
    {
        _nsUri = nsUri;
        _qname = qname;
        _lclName = getLocalName(qname);
        _value = value;
    }


    @Override
    protected void appendToElement(Element parent)
    {
        if (_nsUri == null)
            parent.setAttribute(_qname, _value);
        else
            parent.setAttributeNS(_nsUri, _qname, _value);
    }


    /**
     *  Helper method to produce a SAX <code>Attributes</code> object. This
     *  is called by ElementNode.
     */
    protected void appendToAttributes(AttributesImpl attrs)
    {
        attrs.addAttribute(_nsUri, _lclName, _qname, "", _value);
    }
}
