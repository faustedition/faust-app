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

package de.faustedition.template;

import de.faustedition.graph.NodeWrapperCollection;
import de.faustedition.graph.NodeWrapperCollectionTemplateModel;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class TemplateObjectWrapper extends DefaultObjectWrapper {

	@Override
	public TemplateModel wrap(Object obj) throws TemplateModelException {
		if (obj instanceof NodeWrapperCollection) {
			return new NodeWrapperCollectionTemplateModel((NodeWrapperCollection<?>) obj);
		}
		return super.wrap(obj);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object unwrap(TemplateModel model, Class hint) throws TemplateModelException {
		if (model instanceof NodeWrapperCollectionTemplateModel) {
			return ((NodeWrapperCollectionTemplateModel) model).getCollection();
		}
		return super.unwrap(model, hint);
	}
}
