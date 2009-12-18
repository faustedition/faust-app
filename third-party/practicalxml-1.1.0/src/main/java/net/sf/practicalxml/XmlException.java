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

package net.sf.practicalxml;


/**
 *  An unchecked exception for use by the utility classes, typically to wrap
 *  a checked exception thrown the XML library.
 */
public class XmlException
extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public XmlException(String msg)
    {
        super(msg);
    }

    public XmlException(String msg, Throwable cause)
    {
        super(msg, cause);
    }


    public XmlException(Throwable cause)
    {
        super(cause);
    }
}
