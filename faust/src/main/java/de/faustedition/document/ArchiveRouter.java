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

package de.faustedition.document;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import de.faustedition.ApplicationContextFinder;
import de.faustedition.graph.FaustGraph;
import de.faustedition.template.TemplateRepresentationFactory;
import de.faustedition.xml.NodeListWrapper;
import de.faustedition.xml.XMLStorage;
import de.faustedition.xml.XMLUtil;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.xpath.XPathExpression;
import java.io.IOException;
import java.util.*;

import static de.faustedition.xml.XPathUtil.xpath;

@Component
public class ArchiveRouter extends Router implements InitializingBean {

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public void afterPropertiesSet() throws Exception {
		ApplicationContextFinder<ArchiveResource> archiveResource = new ApplicationContextFinder<ArchiveResource>(applicationContext, ArchiveResource.class);
		attach("", archiveResource);
		attach("{id}", archiveResource);
	}

	@Component
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public static class ArchiveResource extends ServerResource {

		@Autowired
		private XMLStorage xmlStorage;

		@Autowired
		private TemplateRepresentationFactory viewFactory;

		@Autowired
		private FaustGraph graph;

		@Get
		public Representation render() {
			final Map<String, Object> model = new HashMap<String, Object>();
			final String id = (String) getRequestAttributes().get("id");

			final org.w3c.dom.Document archives;
			try {
				archives = XMLUtil.parse(xmlStorage.getInputSource(ArchiveInitializer.ARCHIVE_DESCRIPTOR_URI));
			} catch (SAXException e) {
				throw Throwables.propagate(e);
			} catch (IOException e) {
				throw Throwables.propagate(e);
			}

			if (id == null) {
				model.put("archives", archives.getDocumentElement());
				return viewFactory.create("document/archives", getRequest().getClientInfo(), model);
			}

			Archive archive = graph.getArchives().findById(id);
			if (archive == null) {
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, id);
				return null;
			}

			final SortedSet<Document> archivalUnits = new TreeSet<Document>(new Comparator<Document>() {
				@Override
				public int compare(Document o1, Document o2) {
					final String o1cn = o1.getMetadataValue("callnumber");
					final String o2cn = o2.getMetadataValue("callnumber");
					if (o1cn != null && o2cn != null) {
						int order = o1cn.compareTo(o2cn);
						if (order != 0) return order;
					}
					return o1.getSource().compareTo(o2.getSource());
				}
			});

			Iterables.addAll(archivalUnits, Iterables.filter(archive, Document.class));
			model.put("archivalUnits", archivalUnits);

			final XPathExpression xpathById = xpath("/f:archives/f:archive[@id='" + id + "']");
			final Element archiveData = new NodeListWrapper<Element>(xpathById, archives).singleResult(Element.class);
			if (archiveData == null) {
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, id);
				return null;
			}
			model.put("archive", archiveData);

			return viewFactory.create("document/archive", getRequest().getClientInfo(), model);
		}
	}
}
