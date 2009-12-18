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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathVariableResolver;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.XmlException;



/**
 *  This class simplifies the use of XPath expressions, hiding the factory and
 *  return types, and providing a simple builder-style interface for adding
 *  resolvers. It also maintains the expression in a compiled form, improving
 *  reuse performance.
 */
public class XPathWrapper
{
    private final String _expr;
    private final NamespaceResolver _nsResolver = new NamespaceResolver();
    private Map<QName,Object> _variables = new HashMap<QName,Object>();
    private FunctionResolver _functions = new FunctionResolver();

    private XPathExpression _compiled;


    /**
     *  Creates a new instance, which may then be customized with various
     *  resolvers, and used to evaluate expressions.
     */
    public XPathWrapper(String expr)
    {
        _expr = expr;
    }


//----------------------------------------------------------------------------
//  Public methods
//----------------------------------------------------------------------------

    /**
     *  Adds a namespace binding to this expression. All bindings must be
     *  added prior to the first call to <code>evaluate()</code>.
     *
     *  @param  prefix  The prefix used to reference this namespace in the
     *                  XPath expression. Note that this does <em>not</em>
     *                  need to be the same prefix used by the document.
     *  @param  nsURI   The namespace URI to associate with this prefix.
     *
     *  @return The wrapper, so that calls may be chained.
     */
    public XPathWrapper bindNamespace(String prefix, String nsURI)
    {
        _nsResolver.addNamespace(prefix, nsURI);
        return this;
    }


    /**
     *  Sets the default namespace binding: this will be applied to all
     *  expressions that do not explicitly specify a prefix (although
     *  they must use the colon separating the non-existent prefix from
     *  the element name).
     *
     *  @param  nsURI   The default namespace for this document.
     *
     *  @return The wrapper, so that calls may be chained.
     */
    public XPathWrapper bindDefaultNamespace(String nsURI)
    {
        _nsResolver.setDefaultNamespace(nsURI);
        return this;
    }


    /**
     *  Binds a value to a variable, replacing any previous value for that
     *  variable. Unlike other configuration methods, this may be called
     *  after calling <code>evaluate()</code>; the new values will be used
     *  for subsequent evaluations.
     *
     *  @param  name    The name of the variable; this is turned into a
     *                  <code>QName</code> without namespace.
     *  @param  value   The value of the variable; the XPath evaluator must
     *                  be able to convert this value into a type usable in
     *                  an XPath expression.
     *
     *  @return The wrapper, so that calls may be chained.
     */
    public XPathWrapper bindVariable(String name, Object value)
    {
        return bindVariable(new QName(name), value);
    }


    /**
     *  Binds a value to a variable, replacing any previous value for that
     *  variable. Unlike other configuration methods, this may be called
     *  after calling <code>evaluate()</code>; the new values will be used
     *  for subsequent evaluations.
     *
     *  @param  name    The fully-qualified name of the variable.
     *  @param  value   The value of the variable; the XPath evaluator must
     *                  be able to convert this value into a type usable in
     *                  an XPath expression.
     *
     *  @return The wrapper, so that calls may be chained.
     */
    public XPathWrapper bindVariable(QName name, Object value)
    {
        _variables.put(name, value);
        return this;
    }


    /**
     *  Binds an {@link net.sf.practicalxml.xpath.AbstractFunction} to this
     *  expression. Subsequent calls to this method with the same name will
     *  silently replace the binding.
     *  <p>
     *  Per the JDK documentation, user-defined functions must occupy their
     *  own namespace. You must bind a namespace for this function, and use
     *  the bound prefix to reference it in your expression. Alternatively,
     *  you can call the variant of <code>bindFunction()</code> that binds
     *  a prefix to the function's namespace.
     *
     *  @param  func    The function.
     *
     *  @return The wrapper, so that calls may be chained.
     */
    public XPathWrapper bindFunction(AbstractFunction<?> func)
    {
        _functions.addFunction(func);
        return this;
    }


    /**
     *  Binds an {@link net.sf.practicalxml.xpath.AbstractFunction} to this
     *  expression, along with the prefix used to access that function.
     *  Subsequent calls to this method with the same name will silently
     *  replace the binding.
     *  <p>
     *  Per the JDK documentation, user-defined functions must occupy their
     *  own namespace. This method will retrieve the namespace from the
     *  function, and bind it to the passed prefix.
     *
     *  @param  func    The function.
     *  @param  prefix  The prefix to bind to this function's namespace.
     *
     *  @return The wrapper, so that calls may be chained.
     */
    public XPathWrapper bindFunction(AbstractFunction<?> func, String prefix)
    {
        _functions.addFunction(func);
        return bindNamespace(prefix, func.getNamespaceUri());
    }


    /**
     *  Binds a standard <code>XPathFunction</code> to this expression,
     *  handling any number of arguments. Subsequent calls to this method
     *  with the same name will silently replace the binding.
     *  <p>
     *  Per the JDK documentation, user-defined functions must occupy their
     *  own namespace. If the qualified name that you pass to this method
     *  includes a prefix, the associated namespace will be bound to that
     *  prefix. If not, you must bind the namespace explicitly. In either
     *  case, you must refer to the function in your expression using a
     *  bound prefix.
     *
     *  @param  name    The qualified name for this function. Must contain
     *                  a name and namespace, may contain a prefix.
     *  @param  func    The function to bind to this name.
     *
     *  @return The wrapper, so that calls may be chained.
     */
    public XPathWrapper bindFunction(QName name, XPathFunction func)
    {
        return bindFunction(name, func, 0, Integer.MAX_VALUE);
    }


    /**
     *  Binds a standard <code>XPathFunction</code> to this expression,
     *  handling a specific number of arguments. Subsequent calls to this
     *  method with the same name and arity will silently replace the binding.
     *  <p>
     *  Per the JDK documentation, user-defined functions must occupy their
     *  own namespace. If the qualified name that you pass to this method
     *  includes a prefix, the associated namespace will be bound to that
     *  prefix. If not, you must bind the namespace explicitly. In either
     *  case, you must refer to the function in your expression using a
     *  bound prefix.
     *
     *  @param  name    The qualified name for this function. Must contain
     *                  a name and namespace, may contain a prefix.
     *  @param  func    The function to bind to this name.
     *  @param  arity   The number of arguments accepted by this function.
     *
     *  @return The wrapper, so that calls may be chained.
     */
    public XPathWrapper bindFunction(QName name, XPathFunction func, int arity)
    {
        return bindFunction(name, func, arity, arity);
    }


    /**
     *  Binds a standard <code>XPathFunction</code> to this expression,
     *  handling a specific range of arguments. Subsequent calls to this
     *  method with the same name and range of arguments will silently
     *  replace the binding.
     *  <p>
     *  Per the JDK documentation, user-defined functions must occupy their
     *  own namespace. If the qualified name that you pass to this method
     *  includes a prefix, the associated namespace will be bound to that
     *  prefix. If not, you must bind the namespace explicitly. In either
     *  case, you must refer to the function in your expression using a
     *  bound prefix.
     *
     *  @param  name      The qualified name for this function. Must contain
     *                    a name and namespace, may contain a prefix.
     *  @param  func      The function to bind to this name.
     *  @param  minArity  The minimum number of arguments accepted by this
     *                    function.
     *  @param  maxArity  The maximum number of arguments accepted by this
     *                    function.
     *
     *  @return The wrapper, so that calls may be chained.
     */
    public XPathWrapper bindFunction(QName name, XPathFunction func,
                                     int minArity, int maxArity)
    {
        _functions.addFunction(func, name, minArity, maxArity);
        if (!"".equals(name.getPrefix()))
            bindNamespace(name.getPrefix(), name.getNamespaceURI());
        return this;
    }


    /**
     *  Applies this expression to the the specified node, converting the
     *  resulting <code>NodeList</code> into a <code>java.util.List</code>.
     */
    public List<Node> evaluate(Node context)
    {
        return DomUtil.asList(
                evaluate(context, XPathConstants.NODESET, NodeList.class),
                Node.class);
    }


    /**
     *  Applies this expression to the specified node, requesting the
     *  <code>STRING</code> return type.
     */
    public String evaluateAsString(Node context)
    {
        return evaluate(context, XPathConstants.STRING, String.class);
    }


    /**
     *  Applies this expression to the specified node, requesting the
     *  <code>NUMBER</code> return type.
     */
    public Number evaluateAsNumber(Node context)
    {
        return evaluate(context, XPathConstants.NUMBER, Number.class);
    }


    /**
     *  Applies this expression to the the specified node, requesting the
     *  <code>BOOLEAN</code> return type.
     */
    public Boolean evaluateAsBoolean(Node context)
    {
        return evaluate(context, XPathConstants.BOOLEAN, Boolean.class);
    }


//----------------------------------------------------------------------------
//  Overrides of Object
//----------------------------------------------------------------------------

    /**
     *  Two instances are considered equal if they have the same expression,
     *  namespace mappings, variable mappings, and function mappings. Note
     *  that instances with function mappings are all but guaranteed to be
     *  not-equal, unless you override the <code>equals()</code> method on
     *  the function implementation class.
     */
    @Override
    public final boolean equals(Object obj)
    {
        if (obj instanceof XPathWrapper)
        {
            XPathWrapper that = (XPathWrapper)obj;
            return this._expr.equals(that._expr)
                && this._nsResolver.equals(that._nsResolver)
                && this._variables.equals(that._variables)
                && this._functions.equals(that._functions);
        }
        return false;
    }


    /**
     *  Hash code is driven by the expression, ignoring differences between
     *  namespaces, variables, and functions.
     */
    @Override
    public int hashCode()
    {
        return _expr.hashCode();
    }


    /**
     *  The string value is the expression.
     */
    @Override
    public String toString()
    {
        return _expr;
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    /**
     *  Compiles the expression, if it has not already been compiled. This is
     *  called from the various <code>evaluate</code> methods, and ensures
     *  that the caller has completely configured the wrapper prior to use.
     */
    private void compileIfNeeded()
    {
        if (_compiled != null)
            return;

        try
        {
            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(_nsResolver);
            xpath.setXPathVariableResolver(new MyVariableResolver());
            xpath.setXPathFunctionResolver(_functions);
            _compiled = xpath.compile(_expr);
        }
        catch (XPathExpressionException ee)
        {
            throw new XmlException("unable to compile: " + _expr, ee);
        }
    }


    /**
     *  Compiles and executes the expression in the context of the specified
     *  node, returning the specified result type.
     */
    private <T> T evaluate(Node context, QName returnType, Class<T> castTo)
    {
        compileIfNeeded();
        try
        {
            return castTo.cast(_compiled.evaluate(context, returnType));
        }
        catch (Exception ee)
        {
            throw new XmlException("unable to evaluate: " + _expr, ee);
        }
    }


    /**
     *  Resolver for variable references.
     */
    private class MyVariableResolver
    implements XPathVariableResolver
    {
        public Object resolveVariable(QName name)
        {
            return _variables.get(name);
        }
    }
}
