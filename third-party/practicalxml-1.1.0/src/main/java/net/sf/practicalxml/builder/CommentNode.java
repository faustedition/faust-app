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

import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;


/**
 *  Holds a comment.
 */
public class CommentNode
extends Node
{
    private static final long serialVersionUID = 1L;

    private String  _content;

    public CommentNode(String content)
    {
        _content = content;
    }


    @Override
    protected void appendToElement(Element parent)
    {
        Comment node = parent.getOwnerDocument().createComment(_content);
        parent.appendChild(node);
    }


    @Override
    protected void toSAX(ContentHandler handler) throws SAXException
    {
        if (handler instanceof LexicalHandler)
        {
            ((LexicalHandler)handler).comment(_content.toCharArray(), 0, _content.length());
        }
    }
}
