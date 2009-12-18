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
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import net.sf.practicalxml.DomUtil;


/**
 *  Holds element content. Will be converted to DOM as <code>Text</code>
 *  (not <code>CDATASection</code>).
 */
public class TextNode extends Node
{
    private static final long serialVersionUID = 1L;

    private String  _content;

    public TextNode(String content)
    {
        _content = content;
    }


    @Override
    protected void appendToElement(Element parent)
    {
        DomUtil.appendText(parent, _content);
    }


    @Override
    protected void toSAX(ContentHandler handler) throws SAXException
    {
        handler.characters(_content.toCharArray(), 0, _content.length());
    }
}
