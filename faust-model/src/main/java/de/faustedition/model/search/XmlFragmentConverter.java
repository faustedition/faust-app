package de.faustedition.model.search;

import java.io.ByteArrayInputStream;

import net.sf.practicalxml.ParseUtil;

import org.compass.core.converter.ConversionException;
import org.compass.core.converter.basic.AbstractBasicConverter;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.marshall.MarshallingContext;
import org.xml.sax.InputSource;

public class XmlFragmentConverter extends AbstractBasicConverter<byte[]>
{

	@Override
	protected byte[] doFromString(String str, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) throws ConversionException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected String doToString(byte[] o, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context)
	{
		return ParseUtil.parse(new InputSource(new ByteArrayInputStream(o))).getDocumentElement().getTextContent();
	}

}
