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

package net.sf.practicalxml.converter.bean;

import java.util.EnumSet;
import org.w3c.dom.Element;

import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.converter.ConversionException;
import net.sf.practicalxml.converter.internal.ConversionStrings;
import net.sf.practicalxml.converter.internal.ConversionUtils;
import net.sf.practicalxml.converter.internal.TypeUtils;


/**
 *  Packaging class used for XML output appenders. This class is a temporary
 *  hack, as I move intelligence into {@link Bean2XmlConverter}; the contained
 *  classes will end up in a new package, once I figure out what the package
 *  structure should be.
 */
public abstract class Bean2XmlAppenders
{
    /**
     *  An <code>Appender</code> appends children to a single node of the
     *  output tree. The driver is responsible for creating new appenders
     *  for each compound element, including the root, and providing those
     *  appenders with options to control output generation.
     */
    public interface Appender
    {
        /**
         *  Appends a value element to the current element. Value elements have
         *  associated text, but no other children.
         *
         *  @param name     Name to be associated with the node.
         *  @param klass    Java class for this node. May (depending on options)
         *                  be stored in the <code>type</code> attribute.
         *  @param value    The node's value. May be <code>null</code>, in
         *                  which case the appender decides whether or not
         *                  to actually append the node.
         *
         *  @return The appended element. This is a convenience for subclasses,
         *          which may want to set additional attributes after their
         *          super has done the work of appending the element.
         *  @throws ConversionException if unable to append the node.
         */
        public Element appendValue(String name, Class<?> klass, String value);


        /**
         *  Appends a container element to the current element. Container
         *  elements have other elements as children, and may have a type,
         *  but do not have an associated value.
         */
        public Element appendContainer(String name, Class<?> klass);
    }


    /**
     *  Base class for XML appenders, providing helper methods for subclasses.
     */
    private static abstract class AbstractAppender
    implements Appender
    {
        private EnumSet<Bean2XmlOptions> _options;

        public AbstractAppender(EnumSet<Bean2XmlOptions> options)
        {
            _options = options;
        }

        protected boolean isOptionSet(Bean2XmlOptions option)
        {
            return _options.contains(option);
        }

        protected boolean shouldSkip(Object value)
        {
            return (value == null)
                && !_options.contains(Bean2XmlOptions.NULL_AS_EMPTY)
                && !_options.contains(Bean2XmlOptions.NULL_AS_XSI_NIL);
        }

        protected void setType(Element elem, Class<?> klass)
        {
            if (isOptionSet(Bean2XmlOptions.USE_TYPE_ATTR))
                TypeUtils.setType(elem, klass);
        }

        protected void setValue(Element elem, String value)
        {
            if (value != null)
                DomUtil.setText(elem, value);
            else if (isOptionSet(Bean2XmlOptions.NULL_AS_EMPTY))
                DomUtil.setText(elem, "");
            else if (isOptionSet(Bean2XmlOptions.NULL_AS_XSI_NIL))
                ConversionUtils.setXsiNil(elem, true);
        }
    }


    /**
     *  Basic appender, which appends new elements to a parent.
     */
    public static class BasicAppender
    extends AbstractAppender
    {
        private Element _parent;

        public BasicAppender(Element parent, EnumSet<Bean2XmlOptions> options)
        {
            super(options);
            _parent = parent;
        }

        public Element appendValue(String name, Class<?> klass, String value)
        {
            if (shouldSkip(value))
                return null;

            try
            {
                Element child = DomUtil.appendChildInheritNamespace(_parent, name);
                setType(child, klass);
                setValue(child, value);
                return child;
            }
            catch (Exception ee)
            {
                throw new ConversionException("unable to append child: " + name, _parent, ee);
            }
        }

        public Element appendContainer(String name, Class<?> klass)
        {
            Element child = DomUtil.appendChildInheritNamespace(_parent, name);
            setType(child, klass);
            return child;
        }
    }


    /**
     *  Appender for children of an indexed/iterated item (array, list, or set).
     *  Each element will have an incremented <code>index</code> attribute that
     *  indicates the position of the element within the iteration.
     */
    public static class IndexedAppender
    extends BasicAppender
    {
        private int _index = 0;

        public IndexedAppender(Element parent, EnumSet<Bean2XmlOptions> options)
        {
            super(parent, options);
        }


        @Override
        public Element appendValue(String name, Class<?> klass, String value)
        {
            Element child = super.appendValue(name, klass, value);
            if ((child != null) && isOptionSet(Bean2XmlOptions.USE_INDEX_ATTR))
                ConversionUtils.setAttribute(
                        child,
                        ConversionStrings.AT_ARRAY_INDEX,
                        String.valueOf(_index++));
            return child;
        }
    }


    /**
     *  Appender for children of a <code>Map</code>. Depending on options,
     *  will either create children named after the key, or a generic "data"
     *  child with the key as an attribute.
     */
    public static class MapAppender
    extends BasicAppender
    {
        public MapAppender(Element parent, EnumSet<Bean2XmlOptions> options)
        {
            super(parent, options);
        }


        @Override
        public Element appendValue(String name, Class<?> klass, String value)
        {
            Element child = null;
            if (isOptionSet(Bean2XmlOptions.MAP_KEYS_AS_ELEMENT_NAME))
            {
                child = super.appendValue(name, klass, value);
            }
            else
            {
                child = super.appendValue(ConversionStrings.EL_COLLECTION_ITEM, klass, value);
                if (child != null)
                    ConversionUtils.setAttribute(
                            child,
                            ConversionStrings.AT_MAP_KEY,
                            name);
            }
            return child;
        }
    }


    /**
     *  An appender that sets values directly on the "parent" element. Used for
     *  the conversion root element.
     */
    public static class DirectAppender
    extends AbstractAppender
    {
        private Element _elem;

        public DirectAppender(Element elem, EnumSet<Bean2XmlOptions> options)
        {
            super(options);
            _elem = elem;
        }

        public Element appendValue(String name, Class<?> klass, String value)
        {
            if (!shouldSkip(value))
            {
                setType(_elem, klass);
                setValue(_elem, value);
            }
            return _elem;
        }

        public Element appendContainer(String name, Class<?> klass)
        {
            setType(_elem, klass);
            return _elem;
        }
    }
}