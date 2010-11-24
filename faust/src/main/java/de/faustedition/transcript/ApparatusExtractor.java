package de.faustedition.transcript;

import static de.faustedition.xml.CustomNamespaceMap.TEI_NS_PREFIX;

import java.util.ArrayList;
import java.util.List;

import org.goddag4j.Element;
import org.goddag4j.GoddagNode;
import org.goddag4j.visit.GoddagVisitor;

import com.google.inject.internal.Lists;

import de.faustedition.xml.CustomNamespaceMap;

public class ApparatusExtractor {

	public void extract(Transcript transcript, String sourceRootPrefix, String sourceRootName) {
		Element source = transcript.getTrees().getRoot(sourceRootPrefix, sourceRootName);
		Element apps = transcript.getTrees().getRoot(CustomNamespaceMap.FAUST_NS_PREFIX, "apps");

		final List<Element> appElements = new ArrayList<Element>();
		new GoddagVisitor() {

			public void startElement(Element root, Element element) {
				if ("app".equals(element.getName()) && TEI_NS_PREFIX.equals(element.getPrefix())) {
					appElements.add(element);
				}
			};

		}.visit(source, source);

		for (Element app : appElements) {
			app.copy(source, apps);
			apps.insert(apps, app, null);

			for (GoddagNode appChild : Lists.newArrayList(app.getChildren(source))) {
				if (appChild.getNodeType() != GoddagNode.NodeType.ELEMENT) {
					continue;
				}
				Element appChildElement = (Element) appChild;
				if ("lem".equals(appChildElement.getName()) && TEI_NS_PREFIX.equals(appChildElement.getPrefix())) {
					app.merge(source, appChildElement);
				} else {
					appChildElement.getParent(source).remove(source, appChildElement, true);
				}
			}
		}

	}
}
