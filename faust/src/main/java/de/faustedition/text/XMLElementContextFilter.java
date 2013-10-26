/*
 * Copyright (c) 2013 The Interedition Development Group.
 *
 * This file is part of Interedition Text.
 *
 * Interedition Text is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Interedition Text is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.faustedition.text;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.Deque;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XMLElementContextFilter implements Predicate<TextToken> {

    private final Predicate<? super TextToken> contextStart;
    private final Predicate<? super TextToken> contextEnd;
    private final Predicate<? super TextToken> exclude;
    private final Predicate<? super TextToken> include;

    private final Deque<Boolean> filterContext = Lists.newLinkedList();

    public XMLElementContextFilter(Predicate<TextToken> exclude, Predicate<TextToken> include, Predicate<TextToken> contextStart, Predicate<TextToken> contextEnd) {
        this.exclude = exclude;
        this.include = include;
        this.contextStart = contextStart;
        this.contextEnd = contextEnd;
    }

    public XMLElementContextFilter(Predicate<TextToken> exclude, Predicate<TextToken> include) {
        this(exclude, include, TextAnnotationStart.IS_INSTANCE, TextAnnotationEnd.IS_INSTANCE);
    }

    @Override
    public boolean apply(@Nullable TextToken input) {
        if (contextStart.apply(input)) {
            final boolean parentIncluded = (filterContext.isEmpty() ? true : filterContext.peek());
            filterContext.push(parentIncluded ? !exclude.apply(input) : include.apply(input));
        }

        final boolean accept = (filterContext.isEmpty() || filterContext.peek());

        if (contextEnd.apply(input) && !filterContext.isEmpty()) {
            filterContext.pop();
        }

        return accept;
    }
}
