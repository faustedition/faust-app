package de.faustedition.transcript;

import static de.faustedition.xml.CustomNamespaceMap.TEI_NS_PREFIX;

import java.util.ArrayList;
import java.util.List;

import org.juxtasoftware.goddag.Element;
import org.juxtasoftware.goddag.GoddagNode;
import org.juxtasoftware.goddag.GoddagNodeType;
import org.juxtasoftware.goddag.util.DefaultGoddagEventHandler;

import com.google.inject.internal.Lists;

import de.faustedition.xml.CustomNamespaceMap;

public class ApparatusExtractor {

    public void extract(Transcript transcript, String sourceRootPrefix, String sourceRootName) {
        Element source = transcript.getRoot(sourceRootPrefix, sourceRootName);
        Element apps = transcript.getRoot(CustomNamespaceMap.FAUST_NS_PREFIX, "apps");
        final List<Element> appElements = new ArrayList<Element>();
        source.stream(source, new DefaultGoddagEventHandler() {
            @Override
            public void startElement(Element element) {
                if ("app".equals(element.getName()) && TEI_NS_PREFIX.equals(element.getPrefix())) {
                    appElements.add(element);
                }
            }
        });
        for (Element app : appElements) {
            app.copy(source, apps);
            apps.insert(apps, app, null);

            for (GoddagNode appChild : Lists.newArrayList(app.getChildren(source))) {
                if (appChild.getNodeType() != GoddagNodeType.ELEMENT) {
                    continue;
                }
                Element appChildElement = (Element) appChild;
                if ("lem".equals(appChildElement.getName()) && TEI_NS_PREFIX.equals(appChildElement.getPrefix())) {
                    app.merge(source, appChildElement);
                } else {
                    appChildElement.delete(source);
                }
            }
        }

    }
}
