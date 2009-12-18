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

package net.sf.practicalxml.converter;

import org.w3c.dom.Element;

import net.sf.practicalxml.DomUtil;


/**
 *  A runtime exception thrown for any conversion error. Will always have a
 *  message, and typically contains a wrapped exception. If thrown during
 *  a conversion <code>from</code> XML, should also have the absolute XPath
 *  of the node that caused the problem, and this is appended to the message.
 */
public class ConversionException
extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    private String _xpath;


    public ConversionException(String message, Element elem, Throwable cause)
    {
        super(message, cause);
        _xpath = DomUtil.getAbsolutePath(elem);
    }

    public ConversionException(String message, Element elem)
    {
        super(message);
        _xpath = DomUtil.getAbsolutePath(elem);
    }


    public ConversionException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ConversionException(String message)
    {
        super(message);
    }


    @Override
    public String toString()
    {
        return (_xpath != null)
             ? super.getMessage() + " (" + _xpath + ")"
             : super.getMessage();
    }
}
