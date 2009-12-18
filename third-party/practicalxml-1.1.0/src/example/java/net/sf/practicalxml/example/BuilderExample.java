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

package net.sf.practicalxml.example;

import net.sf.practicalxml.builder.ElementNode;

import static net.sf.practicalxml.builder.XmlBuilder.*;


public class BuilderExample
{
    public static void main(String[] argv)
    throws Exception
    {
        ElementNode root =
            element("albums",
                element("artist",
                    attribute("name", "Anderson, Laurie"),
                    element("album", text("Big Science")),
                    element("album", text("Mister Heartbreak")),
                    element("album", text("Strange Angels"))),
                element("artist",
                    attribute("name", "Becker & Fagan"),
                    element("album", text("The Collection"))),
                element("artist",
                    attribute("name", "Fine Young Cannibals"),
                    element("album", text("The Raw & The Cooked"))));

        System.out.println(root.toString(4));
    }
}
