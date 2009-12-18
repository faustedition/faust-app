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

package net.sf.practicalxml.util;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 *  An implementation of <code>ErrorHandler</code> that does nothing; meant
 *  to be used as the base for a useful implementation, in cases where you
 *  only want to intercept certain conditions (such as validation errors).
 */
public class ErrorHandlerAdapter implements ErrorHandler
{
    public void fatalError(SAXParseException exception) throws SAXException
    {
        // this comment keeps Eclipse happy
    }


    public void error(SAXParseException exception) throws SAXException
    {
        // this comment keeps Eclipse happy
    }


    public void warning(SAXParseException exception) throws SAXException
    {
        // this comment keeps Eclipse happy
    }
}
