package de.faustedition.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.springframework.util.Assert;
import org.w3c.dom.Document;

import de.faustedition.Log;

public class FileSystemXmlStore extends BaseXmlStore {

	@Override
	public Iterator<URI> iterator() {
		return contents(URI.create("")).iterator();
	}

	protected SortedSet<URI> contents(URI uri) {
		try {
			SortedSet<URI> contents = new TreeSet<URI>();
			File file = new File(relativize(uri));
			if (file.isDirectory()) {
				for (String content : file.list()) {
					if (new File(file, content).isDirectory()) {
						URI dir = uri.resolve(new URI(null, "./" + content + "/", null));
						contents.add(dir);
						contents.addAll(contents(dir));
					} else {
						contents.add(uri.resolve(new URI(null, "./" + content, null)));
					}
				}
			}
			return contents;
		} catch (URISyntaxException e) {
			throw Log.fatalError(e);
		}
	}

	public SortedSet<URI> list(URI uri) {
		try {
			SortedSet<URI> contents = new TreeSet<URI>();
			File file = new File(relativize(uri));
			if (file.isDirectory()) {
				for (String content : file.list()) {
					contents.add(uri.resolve(new URI(null, "./" + content + (new File(file, content).isDirectory() ? "/" : ""), null)));
				}
			}
			return contents;
		} catch (URISyntaxException e) {
			throw Log.fatalError(e);
		}
	}

	public void delete(URI uri) {
		uri = relativize(uri);
		Log.LOGGER.debug("Deleting XML resource from file system: {}", uri.toString());
		Assert.isTrue(new File(uri).delete(), "Cannot delete " + uri.toString());
	}

	public Document get(URI uri) throws IOException {
		uri = relativize(uri);
		Log.LOGGER.debug("Getting XML resource from file system: {}", uri.toString());
		FileInputStream in = null;
		try {
			in = new FileInputStream(new File(uri));
			return XmlUtil.parse(in);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	public void put(URI uri, Document document) throws IOException {
		uri = relativize(uri);
		Log.LOGGER.debug("Putting XML document in file system: {}", uri.toString());
		FileOutputStream out = null;
		try {
			File file = new File(uri);
			file.getParentFile().mkdirs();

			XmlUtil.serialize(document, out = new FileOutputStream(file));
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	public Document facsimileReferences() {
		Document facsimileRefs = XmlUtil.documentBuilder().newDocument();
		facsimileRefs.appendChild(facsimileRefs.createElementNS(XmlDocument.FAUST_NS_URI, "facsimileRefs"));
		return facsimileRefs;
	}

	public Document encodingStati() {
		Document encodingStati = XmlUtil.documentBuilder().newDocument();
		encodingStati.appendChild(encodingStati.createElementNS(XmlDocument.FAUST_NS_URI, "encoding"));
		return encodingStati;
	}

	public Document identifiers() {
		Document identifiers = XmlUtil.documentBuilder().newDocument();
		identifiers.appendChild(identifiers.createElementNS(XmlDocument.FAUST_NS_URI, "identifiers"));
		return identifiers;
	}
}
