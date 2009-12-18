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
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


/**
 *  Holds a processing instruction.
 */
public class PINode extends Node
{
    private static final long serialVersionUID = 1L;

    private String _target;
    private String _data;

    public PINode(String target, String data)
    {
        _target = target;
        _data = data;
    }


    @Override
    protected void appendToElement(Element parent)
    {
        ProcessingInstruction pi = parent.getOwnerDocument()
                                   .createProcessingInstruction(_target, _data);
        parent.appendChild(pi);
    }


    @Override
    protected void toSAX(ContentHandler handler) throws SAXException
    {
        handler.processingInstruction(_target, _data);
    }
}
