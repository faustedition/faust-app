package de.swkk.metadata;

import static de.faustedition.xml.XmlDocument.FAUST_NS_URI;

import java.util.LinkedHashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.faustedition.xml.XmlUtil;

public class MetadataRecord extends LinkedHashMap<String, String> {
	public void toXml(Element parent) {
		Document d = parent.getOwnerDocument();
		for (Map.Entry<String, String> entry : entrySet()) {
			Element field = d.createElementNS(FAUST_NS_URI, entry.getKey());
			field.setTextContent(entry.getValue());
			parent.appendChild(field);
		}
	}

	public static MetadataRecord fromXml(Element parent) {
		MetadataRecord record = new MetadataRecord();
		for (Element field : XmlUtil.getChildElements(parent)) {
			if (FAUST_NS_URI.equals(field.getNamespaceURI())
					&& MetadataFieldDefinition.REGISTRY_LOOKUP_TABLE.containsKey(field.getLocalName())) {
				record.put(field.getLocalName(), field.getTextContent());
			}
		}
		return record;
	}

	public void merge(MetadataRecord other) {
		for (Map.Entry<String, String> entry : other.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public String put(String key, String value) {
		if (containsKey(key)) {
			String currentValue = get(key);
			value = currentValue + (currentValue.contains(value) ? "" : ("\n" + value));
		}
		return super.put(key, value);
	}
}
