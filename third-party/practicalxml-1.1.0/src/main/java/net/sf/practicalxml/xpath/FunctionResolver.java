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
import java.util.TreeSet;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;


/**
 *  An <code>XPathFunctionResolver</code> designed for use with subclasses of
 *  {@link AbstractFunction} (but will work with any <code>XPathFunction</code>).
 *  Functions are resolved based on name, namespace, and arity. Where multiple
 *  eligible functions have the same name and namespace, the comparison rules of
 *  <code>AbstractFunction</code> are used to pick the most appropriate function:
 *  smaller range of accepted arguments first, followed by lower minimum argument
 *  count.
 *  <p>
 *  This object follows the builder pattern: <code>addFunction()</code> returns
 *  the object itself, so that calls may be chained.
 */
public class FunctionResolver
implements XPathFunctionResolver
{
    private Map<QName,FunctionHolder> _table = new HashMap<QName,FunctionHolder>();

//----------------------------------------------------------------------------
//  Public Methods
//----------------------------------------------------------------------------

    /**
     *  Adds a subclass of {@link AbstractFunction} to this resolver. This
     *  function provides its own information about name, namespace, and
     *  supported number of arguments.
     *  <p>
     *  If the same function has already been added, will silently replace it.
     *
     *  @return The resolver, so that calls may be chained.
     */
    public FunctionResolver addFunction(AbstractFunction<?> func)
    {
        FunctionHolder holder = _table.get(func.getQName());
        if (holder == null)
        {
            holder = new FunctionHolder(func);
            _table.put(func.getQName(), holder);
        }
        else
        {
            holder.put(func);
        }
        return this;
    }


    /**
     *  Adds a normal <code>XPathFunction</code> to this resolver, without
     *  specifying argument count. This function will be chosen for any number
     *  of arguments, provided that there is not a more-specific binding).
     *  <p>
     *  If a function has already been added with the same name and argument
     *  range, this call will silently replace it.
     *  <p>
     *  Note: the resolver wraps the passed function with a decorator that
     *  allows it to be managed. The resolver will return this decorator
     *  object rather than the original function.
     *
     *  @return The resolver, so that calls may be chained.
     */
    public FunctionResolver addFunction(XPathFunction func, QName name)
    {
        return addFunction(func, name, 0, Integer.MAX_VALUE);
    }


    /**
     *  Adds a normal <code>XPathFunction</code> to this resolver. Specifies
     *  the exact number of arguments for which this function will be chosen.
     *  <p>
     *  If a function has already been added with the same name and argument
     *  range, this call will silently replace it.
     *  <p>
     *  Note: the resolver wraps the passed function with a decorator that
     *  allows it to be managed. The resolver will return this decorator
     *  object rather than the original function.
     *
     *  @return The resolver, so that calls may be chained.
     */
    public FunctionResolver addFunction(XPathFunction func, QName name, int argCount)
    {
        return addFunction(func, name, argCount, argCount);
    }


    /**
     *  Adds a normal <code>XPathFunction</code> to this resolver. Specifies
     *  the minimum and maximum number of arguments for which this function
     *  will be chosen.
     *  <p>
     *  If a function has already been added with the same name and argument
     *  range, this call will silently replace it.
     *  <p>
     *  Note: the resolver wraps the passed function with a decorator that
     *  allows it to be managed. The resolver will return this decorator
     *  object rather than the original function.
     *
     *  @return The resolver, so that calls may be chained.
     */
    public FunctionResolver addFunction(XPathFunction func, QName name, int minArgCount, int maxArgCount)
    {
        return addFunction(new StandardFunctionAdapter(func, name, minArgCount, maxArgCount));
    }


//----------------------------------------------------------------------------
//  XPathFunctionResolver
//----------------------------------------------------------------------------

    /**
     *  Picks the "most eligble" function of those bound to this resolver.
     *  If unable to find any function that matches, returns <code>null</code>.
     */
    public XPathFunction resolveFunction(QName functionName, int arity)
    {
        FunctionHolder holder = _table.get(functionName);
        return (holder != null) ? holder.get(arity)
                                : null;
    }


//----------------------------------------------------------------------------
//  Object overrides
//----------------------------------------------------------------------------

    /**
     *  Two instances are equal if they have the same number of functions,
     *  and each has a counterpart with the same QName and arity range (but
     *  not the same implementation).
     */
    @Override
    public final boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        else if (obj instanceof FunctionResolver)
            return this._table.equals(((FunctionResolver)obj)._table);
        else
            return false;
    }


    @Override
    public final int hashCode()
    {
        // I suspect this is a very inefficient way to compute hashcode,
        // but this object should never be stored in a hashing structure
        // -- if you need to do that, come up with a better implementation
        return _table.keySet().hashCode();
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    /**
     *  Holder for AbstractFunction instances. Can deal with 1 or N, and
     *  pick the appropriate one for a particular evaluation.
     */
    private static class FunctionHolder
    {
        private AbstractFunction<?> _onlyOne;
        private TreeSet<AbstractFunction<?>> _hasMany;

        public FunctionHolder(AbstractFunction<?> initial)
        {
            _onlyOne = initial;
        }

        public void put(AbstractFunction<?> func)
        {
            if (_hasMany != null)
            {
                // remove old implementation if it exists
                _hasMany.remove(func);
                _hasMany.add(func);
            }
            else if (_onlyOne.equals(func))
            {
                _onlyOne = func;
            }
            else
            {
                _hasMany = new TreeSet<AbstractFunction<?>>();
                _hasMany.add(func);
                _hasMany.add(_onlyOne);
                _onlyOne = null;
            }
        }

        public AbstractFunction<?> get(int arity)
        {
            if (_onlyOne != null)
            {
                return (_onlyOne.isArityMatch(arity))
                       ? _onlyOne
                       : null;
            }

            for (AbstractFunction<?> func : _hasMany)
            {
                if (func.isArityMatch(arity))
                    return func;
            }

            return null;
        }

        @Override
        public boolean equals(Object obj)
        {
            // if this ever fails, someone needs to fix their code
            FunctionHolder that = (FunctionHolder)obj;

            return (_onlyOne != null)
                 ? this._onlyOne.equals(that._onlyOne)
                 : this._hasMany.equals(that._hasMany);
        }

        @Override
        public int hashCode()
        {
            // I know this will never be stored as the key of a hash-table,
            // but I want to keep static analysis tools quiet
            return 0;
        }
    }


    /**
     *  Adapter for standard XPathFunction instances, allowing them to be
     *  managed by this resolver.
     */
    private static class StandardFunctionAdapter
    extends AbstractFunction<Object>
    {
        final XPathFunction _func;

        public StandardFunctionAdapter(XPathFunction func, QName name, int minArgCount, int maxArgCount)
        {
            super(name, minArgCount, maxArgCount);
            _func = func;
        }

        @Override
        public Object evaluate(List args)
        throws XPathFunctionException
        {
            return _func.evaluate(args);
        }
    }
}
