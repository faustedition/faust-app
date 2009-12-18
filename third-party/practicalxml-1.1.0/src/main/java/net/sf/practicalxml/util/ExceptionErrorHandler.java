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

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.practicalxml.XmlException;


/**
 *  An error handler that throws <code>XmlException</code> on any error,
 *  and maintains a list of warnings. This is used by <code>ParseUtil</code>,
 *  to avoid the logging of the default error handler.
 */
public class ExceptionErrorHandler
implements ErrorHandler
{
    private List<SAXParseException> _warnings = new ArrayList<SAXParseException>();


    public void error(SAXParseException e) throws SAXException
    {
        throw new XmlException("unable to parse", e);
    }

    public void fatalError(SAXParseException e) throws SAXException
    {
        throw new XmlException("unable to parse", e);
    }

    public void warning(SAXParseException e) throws SAXException
    {
        _warnings.add(e);
    }

    /**
     *  Returns the list of warnings generated during parsing. May be empty,
     *  will not be <code>null</code>.
     */
    public List<SAXParseException> getWarnings()
    {
        return _warnings;
    }

}
