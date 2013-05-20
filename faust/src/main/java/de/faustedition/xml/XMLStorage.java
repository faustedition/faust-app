package de.faustedition.xml;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.annotation.Nullable;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class XMLStorage implements Iterable<FaustURI> {
	private final Pattern xmlFilenamePattern = Pattern.compile("[^\\.]+\\.[xX][mM][lL]$");


	private final File storageDirectory;
	private final String storagePath;


    public XMLStorage(File storageDirectory) {
        this.storageDirectory = storageDirectory;
        this.storagePath = storageDirectory.getAbsolutePath();
    }

	@Override
	public Iterator<FaustURI> iterator() {
		return iterate(storageDirectory).iterator();
	}

	public Iterable<FaustURI> iterate(FaustURI base) {
		final File baseFile = toFile(base);
		Preconditions.checkArgument(baseFile.isDirectory(), baseFile.getAbsolutePath() + " is not a valid base directory");
		return iterate(baseFile);
	}

	protected Iterable<FaustURI> iterate(File base) {
		final List<File> files = new LinkedList<File>();
		listRecursively(files, base);
        return Iterables.transform(files, new Function<File, FaustURI>() {
            @Nullable
            @Override
            public FaustURI apply(@Nullable File input) {
                return toUri(input);
            }
        });
    }

	private void listRecursively(List<File> contents, File base) {
		for (File contained : base.listFiles()) {
			if (contained.isDirectory()) {
				listRecursively(contents, contained);
			} else if (contained.isFile() && xmlFilenamePattern.matcher(contained.getName()).matches()) {
				contents.add(contained);
			}
		}
	}

	public InputSource getInputSource(FaustURI uri) throws IOException {
		InputSource is = new InputSource(uri.toString());
		is.setByteStream(new FileInputStream(toFile(uri)));
		return is;
	}

	public void put(FaustURI uri, Document xml) throws TransformerException {
		XMLUtil.serialize(xml, toFile(uri));
	}

	public FaustURI walk (Deque<String> path) {
		return walk(path, null);
	}
	
	public FaustURI walk(Deque<String> path, Deque<String> walked) throws IllegalArgumentException {
		if (path.size() == 0) {
			return null;
		}

		FaustURI uri = new FaustURI(FaustAuthority.XML, "/");
		while (path.size() > 0) {
			String next = path.pop();
			final FaustURI nextURI = uri.resolve(next);
			if (walked != null)
				walked.add(next);
			if (isDirectory(nextURI)) {
				uri = new FaustURI(URI.create(nextURI.toString() + "/"));
				continue;
			} else if (isResource(nextURI)) {
				uri = nextURI;
				break;
			} else {
				return null;
			}
		}
		return (isResource(uri) ? uri : null);
	}

	public boolean isDirectory(FaustURI uri) {
		final File file = toFile(uri);
		return file.isDirectory() && isInStore(file);
	}

	public boolean isResource(FaustURI uri) {
		final File file = toFile(uri);
		return file.isFile() && isInStore(file) && xmlFilenamePattern.matcher(file.getName()).matches();
	}

	protected File toFile(FaustURI uri) {
		Preconditions.checkArgument(FaustAuthority.XML == uri.getAuthority(), uri + " not valid");
		final File file = new File(storageDirectory, uri.getPath());
		final String filePath = file.getAbsolutePath();
		Preconditions.checkArgument(filePath.startsWith(storagePath), filePath + " is not in XML storage");
		return file;
	}

	protected FaustURI toUri(File file) {
		final String filePath = file.getAbsolutePath();
		Preconditions.checkArgument(isInStore(file), filePath + " not in XML store");
		return new FaustURI(FaustAuthority.XML, filePath.substring(storagePath.length()));
	}

	protected boolean isInStore(File file) {
		return file.getAbsolutePath().startsWith(storagePath);
	}

}
