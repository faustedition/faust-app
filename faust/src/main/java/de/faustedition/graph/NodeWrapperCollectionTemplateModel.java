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

package de.faustedition.graph;

import freemarker.ext.beans.BeanModel;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.*;

public class NodeWrapperCollectionTemplateModel implements TemplateCollectionModel, TemplateHashModel {

	private final NodeWrapperCollection<?> collection;
	private TemplateCollectionModel collectionModel;
	private BeanModel beanModel;

	public NodeWrapperCollectionTemplateModel(NodeWrapperCollection<?> collection) {
		this.collection = collection;
	}

	public NodeWrapperCollection<?> getCollection() {
		return collection;
	}

	@Override
	public TemplateModel get(String key) throws TemplateModelException {
		return beanModel().get(key);
	}

	@Override
	public boolean isEmpty() throws TemplateModelException {
		return beanModel().isEmpty();
	}

	@Override
	public TemplateModelIterator iterator() throws TemplateModelException {
		return collectionModel().iterator();
	}

	protected TemplateCollectionModel collectionModel() {
		if (collectionModel == null) {
			collectionModel = new SimpleCollection(collection);
		}
		return collectionModel;
	}

	protected BeanModel beanModel() throws TemplateModelException {
		if (beanModel == null) {
			beanModel = new BeanModel(collection, BeansWrapper.getDefaultInstance());
		}
		return beanModel;
	}
}
