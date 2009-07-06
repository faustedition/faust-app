package de.faustedition.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import de.faustedition.model.HierarchyNode;
import de.faustedition.model.metadata.MetadataField;
import de.faustedition.model.metadata.MetadataFieldGroup;
import de.faustedition.model.metadata.MetadataValue;
import de.faustedition.model.service.HierarchyManager;
import de.faustedition.model.service.MetadataManager;

@Controller
@RequestMapping("/metadata/**")
public class MetadataController {
	@Autowired
	private MetadataManager metadataManager;

	@Autowired
	private HierarchyManager hierarchyManager;

	@RequestMapping
	public ModelAndView index(HttpServletRequest request) {
		HierarchyNode node = hierarchyManager.loadByPath(extractNodePath(request));

		ModelAndView modelAndView = new ModelAndView("metadata");
		modelAndView.addObject("node", node);
		modelAndView.addObject("metadata", new MetadataModel(metadataManager.findMetadataValues(node)));
		modelAndView.addObject("children", hierarchyManager.findChildren(node));
		return modelAndView;
	}

	public String extractNodePath(HttpServletRequest request) {
		return StringUtils.strip(StringUtils.removeStart(StringUtils.defaultString(request.getPathInfo()), "/metadata/"), "/");
	}

	public static class HierarchyNodeModel extends HierarchyNode {

		public HierarchyNodeModel(HierarchyNode node) {
			super(node);
		}

		public Iterator<HierarchyNodePathComponent> getPathList() {
			final Iterator<String> pathIterator = Arrays.asList(getPathComponents()).iterator();
			final StringBuilder pathBuilder = new StringBuilder();

			return new Iterator<HierarchyNodePathComponent>() {

				public boolean hasNext() {
					return pathIterator.hasNext();
				}

				public HierarchyNodePathComponent next() {
					String pathComponent = pathIterator.next();
					pathBuilder.append((pathBuilder.length() == 0 ? "" : "/") + pathComponent);

					return new HierarchyNodePathComponent(pathBuilder.toString(), pathComponent);
				}

				public void remove() {
				}
			};
		}

		public boolean isRoot() {
			return getName().length() == 0;
		}
	}

	public static class HierarchyNodePathComponent {
		private String fullPath;
		private String name;

		public HierarchyNodePathComponent(String fullPath, String name) {
			this.fullPath = fullPath;
			this.name = name;
		}

		public String getFullPath() {
			return fullPath;
		}

		public String getName() {
			return name;
		}

		public String getHref() {
			try {
				return StringUtils.replace(URLEncoder.encode(fullPath, "UTF-8"), "/", "%2EF");
			} catch (UnsupportedEncodingException e) {
				return null;
			}
		}
	}

	public static class MetadataModel {
		private SortedSet<MetadataValue> metadataValues = new TreeSet<MetadataValue>(new Comparator<MetadataValue>() {

			public int compare(MetadataValue o1, MetadataValue o2) {
				MetadataField field1 = o1.getField();
				MetadataField field2 = o2.getField();

				if (field1.getGroup().equals(field2.getGroup())) {
					return field1.getFieldOrder() - field2.getFieldOrder();
				}

				return field1.getGroup().getGroupOrder() - field2.getGroup().getGroupOrder();
			}

		});

		private Map<String, Integer> groupSizes = new HashMap<String, Integer>();

		public MetadataModel(List<MetadataValue> valueList) {
			for (MetadataValue value : valueList) {
				MetadataFieldGroup group = value.getField().getGroup();
				if (!groupSizes.containsKey(group.getName())) {
					groupSizes.put(group.getName(), 1);
				} else {
					groupSizes.put(group.getName(), groupSizes.get(group.getName()) + 1);
				}
				metadataValues.add(value);
			}
		}

		public SortedSet<MetadataValue> getMetadataValues() {
			return metadataValues;
		}

		public Map<String, Integer> getGroupSizes() {
			return groupSizes;
		}
	}
}
