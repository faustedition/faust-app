package de.faustedition.transcript;

import static de.faustedition.xml.CustomNamespaceMap.TEI_NS_PREFIX;

import java.util.ArrayList;
import java.util.List;

import org.goddag4j.Element;
import org.goddag4j.GoddagNode;
import org.goddag4j.visit.GoddagVisitor;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import com.google.common.collect.Lists;

import de.faustedition.xml.CustomNamespaceMap;

public class ApparatusExtractor {

	public void extract(GoddagTranscript transcript) {
		final GraphDatabaseService db = transcript.node.getGraphDatabase();
		final Transaction tx = db.beginTx();
		try {
			final Element source = transcript.getDefaultRoot();
			final Element apps = transcript.getTrees().getRoot(CustomNamespaceMap.FAUST_NS_PREFIX, "apps");
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
					final Element rdg = (Element) appChild;
					if ("lem".equals(rdg.getName()) && TEI_NS_PREFIX.equals(rdg.getPrefix())) {
						app.merge(source, rdg);
					} else {
						rdg.getParent(source).remove(source, rdg, true);
					}
				}
			}
			tx.success();
		} finally {
			tx.finish();
		}

	}
}
