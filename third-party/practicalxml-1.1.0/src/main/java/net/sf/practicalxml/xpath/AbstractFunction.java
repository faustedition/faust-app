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

package net.sf.practicalxml.xpath;

import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *  A base class for writing XPath functions that replaces much of the
 *  boilerplate code using a "template method" approach. Execution goes
 *  through the following three stages:
 *  <ol>
 *  <li> Initialization: the subclass method {@link #init} is invoked.
 *  <li> Input: for each argument, the type-appropriate {@link #processArg}
 *       method is called.
 *  <li> Completion: the subclass method {@link #getResult} is invoked, and
 *       its return value is returned from {@link #evaluate}.
 *  </ol>
 *  <p>
 *  Subclasses define their name, namespace, and the number of arguments
 *  they accept (which may be undefined).
 *  <p>
 *  To support thread safety, subclasses are expected to make use of a helper
 *  object that holds intermediate results. The helper object is created by
 *  <code>init()</code>, is an argument to and return from <code>processArg()
 *  </code>, and is an argument to <code>getResult()</code>. Subclasses are
 *  parameterized with the helper object type, and the default behavior of
 *  <code>getResult()</code> is to return the helper (this makes life easier
 *  for a function that takes zero or one arguments).
 *  <p>
 *  <em>Note:</em> in JDK 1.5, DOM objects implement both <code>Node</code>
 *  and <code>NodeList</code>. While this means that  <code>processArg(Node)
 *  </code> would never be called as part of the normal dispatch loop (unless
 *  the implementation changes), it <em>is</em> called as part of the default
 *  behavior of <code>processArg(NodeList)</code>, and is given the first
 *  element in the list.
 */
public class AbstractFunction<T>
implements XPathFunction, Comparable<AbstractFunction<?>>
{
    /**
     *  Qualified name of this function.
     */
    final private QName _qname;


    /**
     *  Minimum number of arguments, 0 if no minimum.
     */
    final private int _minArgCount;


    /**
     *  Maximum number of arguments, Integer.MAX_VALUE if no maximum.
     */
    final private int _maxArgCount;


    /**
     *  Constructor for a function that can take any number of arguments.
     */
    protected AbstractFunction(String nsUri, String localName)
    {
        this(nsUri, localName, 0, Integer.MAX_VALUE);
    }


    /**
     *  Constructor for a function that has a fixed number of arguments.
     */
    protected AbstractFunction(String nsUri, String localName, int numArgs)
    {
        this(nsUri, localName, numArgs, numArgs);
    }


    /**
     *  Constructor for a function that has a variable number of arguments.
     */
    protected AbstractFunction(String nsUri, String localName, int minArgs, int maxArgs)
    {
        this(new QName(nsUri, localName), minArgs, maxArgs);
    }


    /**
     *  Base constructor.
     */
    protected AbstractFunction(QName qname, int minArgs, int maxArgs)
    {
        _qname = qname;
        _minArgCount = minArgs;
        _maxArgCount = maxArgs;
    }


//----------------------------------------------------------------------------
//  Public methods
//----------------------------------------------------------------------------

    /**
     *  Returns the qualified name of this function, consisting of name and
     *  namespace (but not prefix).
     */
    public QName getQName()
    {
        return _qname;
    }


    /**
     *  Returns the namespace URI for this function.
     */
    public String getNamespaceUri()
    {
        return _qname.getNamespaceURI();
    }


    /**
     *  Returns the name of this function.
     */
    public String getName()
    {
        return _qname.getLocalPart();
    }


    /**
     *  Returns the minimum number of arguments handled by this function.
     */
    public int getMinArgCount()
    {
        return _minArgCount;
    }


    /**
     *  Returns the maximum number of arguments handled by this function.
     */
    public int getMaxArgCount()
    {
        return _maxArgCount;
    }


    /**
     *  Determines whether this function is a match for a call to
     *  <code>XPathFunctionResolver.resolveFunction()</code>.
     */
    public boolean isMatch(QName qname, int arity)
    {
        return _qname.equals(qname)
            && (arity >= _minArgCount)
            && (arity <= _maxArgCount);
    }


    /**
     *  Determines whether this function can handle the specified number of
     *  arguments. This is used by {@link FunctionResolver}, which already
     *  knows that the QName matches.
     */
    public boolean isArityMatch(int arity)
    {
        return (arity >= _minArgCount)
            && (arity <= _maxArgCount);
    }


//----------------------------------------------------------------------------
//  Implementation of Comparable, Object overrides
//----------------------------------------------------------------------------

    /**
     *  Instances of this class implement <code>Comparable</code> in order to
     *  support a function resolver picking the most appropriate instance for
     *  a particular invocation. Comparison starts with namespace and name,
     *  using <code>String.compareTo()</code>. If two instances have the same
     *  namespace and name, they are compared based on the number of arguments
     *  they accept:
     *  <ul>
     *  <li> Instances that accept fewer arguments are considered less-than
     *       instances that accept more.
     *  <li> If two instances accept the same number of arguments, the instance
     *       with the lower minimum argument count is less-than that with the
     *       higher count.
     *  </ul>
     */
    public int compareTo(AbstractFunction<?> that)
    {
        int result = this._qname.getNamespaceURI().compareTo(that._qname.getNamespaceURI());
        if (result != 0)
            return (result < 0) ? -1 : 1;

        result = this._qname.getLocalPart().compareTo(that._qname.getLocalPart());
        if (result != 0)
            return (result < 0) ? -1 : 1;

        result = (this._maxArgCount - this._minArgCount) - (that._maxArgCount - that._minArgCount);
        if (result != 0)
            return (result < 0) ? -1 : 1;

        result = this._minArgCount - that._minArgCount;
        if (result != 0)
            return (result < 0) ? -1 : 1;

        return result;
    }


    /**
     *  Two instances are considered equal if they have the same qualified
     *  name and accept the same range of arguments. This supports use with
     *  the collections framework, which is the only place that you should
     *  be checking equality.
     */
    @Override
    public final boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        else if (obj instanceof AbstractFunction<?>)
        {
            AbstractFunction<?> that = (AbstractFunction<?>)obj;
            return this._qname.equals(that._qname)
                && this._minArgCount == that._minArgCount
                && this._maxArgCount == that._maxArgCount;
        }
        else
            return false;
    }


    /**
     *  Hashcode is based on the qualified name, does not consider argument
     *  count.
     */
    @Override
    public final int hashCode()
    {
        return _qname.hashCode();
    }


//----------------------------------------------------------------------------
//  Implementation of XPathFunction
//----------------------------------------------------------------------------

    /**
     *  Invokes methods defined by the subclasses to evaluate the function.
     *  Will invoke the appropriate <code>processArg()</code> method in turn
     *  for each argument, then invokes <code>getResult()</code> to retrieve
     *  the function's result.
     *
     *   @throws XPathFunctionException if the supplied argument list does
     *           not match the min/max for this function, or if any called
     *           method throws an exception.
     */
    public Object evaluate(List args)
    throws XPathFunctionException
    {
        if (args == null)
            args = Collections.EMPTY_LIST;

        if ((args.size() < _minArgCount) || (args.size() > _maxArgCount))
            throw new XPathFunctionException("illegal argument count: " + args.size());

        try
        {
            T helper = init();
            int idx = 0;
            for (Object arg : args)
            {
                if (arg instanceof String)
                    helper = processArg(idx, (String)arg, helper);
                else if (arg instanceof Number)
                    helper = processArg(idx, (Number)arg, helper);
                else if (arg instanceof NodeList)
                    helper = processArg(idx, (NodeList)arg, helper);
                else if (arg instanceof Node)
                    helper = processArg(idx, (Node)arg, helper);
                else if (arg == null)
                    helper = processNullArg(idx, helper);
                else
                    helper = processUnexpectedArg(idx, arg, helper);
                idx++;
            }
            return getResult(helper);
        }
        catch (Exception e)
        {
            throw new XPathFunctionException(e);
        }
    }


//----------------------------------------------------------------------------
//  Subclasses override these methods
//----------------------------------------------------------------------------

    /**
     *  Creates a helper object to preserve intermediate results. This object
     *  is passed to <code>processArg()</code> and <code>getResult()</code>.
     *  <p>
     *  The default implementation returns <code>null</code>.
     *  <p>
     *  Subclasses are permitted to throw any exception; it will be wrapped in
     *  an <code>XPathFunctionException</code>.
     */
    protected T init()
    throws Exception
    {
        return null;
    }


    /**
     *  Processes a String argument.
     *
     *  @param  index   Index of the argument, numbered from 0.
     *  @param  value   Value of the argument.
     *  @param  helper  Helper object to preserve intermediate results.
     *
     *  @return The helper object (subclasses may return a replacement).
     *
     *  @throws Subclasses are permitted to throw any exception. It will be
     *          wrapped in <code>XPathFunctionException</code> and cause
     *          function processing to abort.
     */
    protected T processArg(int index, String value, T helper)
    throws Exception
    {
        return helper;
    }


    /**
     *  Processes a Number argument. Per the XPath spec, this should be a
     *  <code>Double</code>
     *
     *  @param  index   Index of the argument, numbered from 0.
     *  @param  value   Value of the argument.
     *  @param  helper  Helper object to preserve intermediate results.
     *
     *  @return The helper object (subclasses may return a replacement).
     *
     *  @throws Subclasses are permitted to throw any exception. It will be
     *          wrapped in <code>XPathFunctionException</code> and cause
     *          function processing to abort.
     */
    protected T processArg(int index, Number value, T helper)
    throws Exception
    {
        return helper;
    }


    /**
     *  Processes a Boolean argument.
     *
     *  @param  index   Index of the argument, numbered from 0.
     *  @param  value   Value of the argument.
     *  @param  helper  Helper object to preserve intermediate results.
     *
     *  @return The helper object (subclasses may return a replacement).
     *
     *  @throws Subclasses are permitted to throw any exception. It will be
     *          wrapped in <code>XPathFunctionException</code> and cause
     *          function processing to abort.
     */
    protected T processArg(int index, Boolean value, T helper)
    throws Exception
    {
        return helper;
    }


    /**
     *  Processes a Node argument. This function will be invoked by the
     *  default implementation of <code>processArg(NodeList)</code>.
     *
     *  @param  index   Index of the argument, numbered from 0.
     *  @param  value   Value of the argument. May be <code>null</code>.
     *  @param  helper  Helper object to preserve intermediate results.
     *
     *  @return The helper object (subclasses may return a replacement).
     *
     *  @throws Subclasses are permitted to throw any exception. It will be
     *          wrapped in <code>XPathFunctionException</code> and cause
     *          function processing to abort.
     */
    protected T processArg(int index, Node value, T helper)
    throws Exception
    {
        return helper;
    }


    /**
     *  Processes a NodeList argument.
     *  <p>
     *  The default implementation calls <code>processArg(Node)</code> with
     *  the first element in the list (<code>null</code> if the list is empty).
     *
     *  @param  index   Index of the argument, numbered from 0.
     *  @param  value   Value of the argument.
     *  @param  helper  Helper object to preserve intermediate results.
     *
     *  @return The helper object (subclasses may return a replacement).
     *
     *  @throws Subclasses are permitted to throw any exception. It will be
     *          wrapped in <code>XPathFunctionException</code> and cause
     *          function processing to abort.
     */
    protected T processArg(int index, NodeList value, T helper)
    throws Exception
    {
        return processArg(index, value.item(0), helper);
    }


    /**
     *  Processes a <code>null</code> argument &mdash; it's unclear whether
     *  this can ever happen. The default implementation throws <code>
     *  IllegalArgumentException</code>.
     *
     *  @param  index   Index of the argument, numbered from 0.
     *  @param  helper  Helper object to preserve intermediate results.
     *
     *  @return The helper object (subclasses may return a replacement).
     *
     *  @throws Subclasses are permitted to throw any exception. It will be
     *          wrapped in <code>XPathFunctionException</code> and cause
     *          function processing to abort.
     */
    protected T processNullArg(int index, T helper)
    throws Exception
    {
        throw new IllegalArgumentException("null argument: " + index);
    }


    /**
     *  Processes an argument that is not one of the defined types for XPath
     *  evaluation &mdash; it is unclear whether this can actually happen.
     *  The default implementation throws <code>IllegalArgumentException</code>.
     *
     *  @param  index   Index of the argument, numbered from 0.
     *  @param  value   The argument value
     *  @param  helper  Helper object to preserve intermediate results.
     *
     *  @return The helper object (subclasses may return a replacement).
     *
     *  @throws Subclasses are permitted to throw any exception. It will be
     *          wrapped in <code>XPathFunctionException</code> and cause
     *          function processing to abort.
     */
    protected T processUnexpectedArg(int index, Object value, T helper)
    throws Exception
    {
        throw new IllegalArgumentException(
                "unexpected argument: " + index
                + " (" + value.getClass().getName() + ")");
    }


    /**
     *  Returns the result of this invocation.
     *
     *  @param  helper  Helper object to preserve intermediate results.
     *
     *  @return The helper object (subclasses may return whatever they want).
     *
     *  @throws Subclasses are permitted to throw any exception. It will be
     *          wrapped in <code>XPathFunctionException</code> and cause
     *          function processing to abort.
     */
    protected Object getResult(T helper)
    throws Exception
    {
        return helper;
    }
}
