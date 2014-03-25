/*
 * Copyright (c) 2014 Faust Edition development team.
 *
 * This file is part of the Faust Edition.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.faustedition;

import java.util.ArrayDeque;
import java.util.Arrays;

import javax.annotation.Nullable;

import org.restlet.data.Reference;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Path extends ArrayDeque<String> {

	public Path(String path) {
		super(Lists.newArrayList(Iterables.filter(Arrays.asList(path.replaceAll("^/+", "").replaceAll("/+$", "").split("/+")), new Predicate<String>() {
			@Override
			public boolean apply(@Nullable String input) {
				return !input.isEmpty();
			}
		})));
	}

	public Path(Reference reference) {
		this(Objects.firstNonNull(reference.getPath(true), ""));
	}

	public static Path relativeTo(Reference reference) {
		final Reference relativeRef = reference.getRelativeRef();
		return (".".equals(relativeRef.getPath()) ? new Path("") : new Path(relativeRef));
	}
}
