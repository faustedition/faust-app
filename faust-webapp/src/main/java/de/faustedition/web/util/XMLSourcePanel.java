package de.faustedition.web.util;

import java.io.UnsupportedEncodingException;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.w3c.dom.Document;

import de.faustedition.util.ErrorUtil;
import de.faustedition.util.XMLUtil;

public class XMLSourcePanel extends Panel
{

	public XMLSourcePanel(String id, byte[] documentData) {
		this(id, XMLUtil.parse(documentData));
	}
	public XMLSourcePanel(String id, Document document)
	{
		super(id);
		try
		{
			add(new Label("source", new String(XMLUtil.serialize(document, true), "UTF-8")));
		} catch (UnsupportedEncodingException e)
		{
			throw ErrorUtil.fatal("No UTF-8 encoding support", e);
		}
	}

}
