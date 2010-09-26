package de.faustedition.xml;

import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.io.filefilter.FileFilterUtils.andFileFilter;
import static org.apache.commons.io.filefilter.FileFilterUtils.fileFileFilter;
import static org.apache.commons.io.filefilter.FileFilterUtils.suffixFileFilter;
import static org.apache.commons.io.filefilter.FileFilterUtils.trueFileFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.neo4j.helpers.collection.IterableWrapper;
import org.xml.sax.InputSource;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;

@Singleton
public class XMLStorage implements Iterable<FaustURI> {
    private final File storageDirectory;
    private final String storagePath;

    @Inject
    public XMLStorage(@Named("xml.home") String dbDirectory) {
        this.storageDirectory = new File(dbDirectory);
        Preconditions.checkArgument(this.storageDirectory.isDirectory(), storageDirectory.getAbsolutePath() + " is a directory");
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

    @SuppressWarnings("unchecked")
    protected Iterable<FaustURI> iterate(File base) {
        return new FileToUriWrapper(listFiles(base, XML_FILE_FILTER, trueFileFilter()));
    }

    public InputSource getInputSource(FaustURI uri) throws IOException {
        InputSource is = new InputSource(uri.toString());
        is.setByteStream(new FileInputStream(toFile(uri)));
        return is;
    }

    public boolean isDirectory(FaustURI uri) {
        return toFile(uri).isDirectory();
    }

    public boolean isResource(FaustURI uri) {
        return XML_FILE_FILTER.accept(toFile(uri));
    }

    protected File toFile(FaustURI uri) {
        Preconditions.checkArgument(FaustAuthority.XML == uri.getAuthority(), uri + " not valid");
        final File file = new File(storageDirectory, uri.getPath());
        Preconditions.checkArgument(IN_STORE_FILTER.accept(file), file.getAbsolutePath() + " is not in XML storage");
        return file;
    }

    protected FaustURI toUri(File file) {
        final String filePath = file.getAbsolutePath();
        Preconditions.checkArgument(filePath.startsWith(storagePath), filePath + " not in XML store");
        return new FaustURI(FaustAuthority.XML, filePath.substring(storagePath.length()));
    }

    private class FileToUriWrapper extends IterableWrapper<FaustURI, File> {

        private FileToUriWrapper(Iterable<File> iterableToWrap) {
            super(iterableToWrap);
        }

        @Override
        protected FaustURI underlyingObjectToObject(File file) {
            return toUri(file);
        }
    }

    private final IOFileFilter IN_STORE_FILTER = new IOFileFilter() {

        @Override
        public boolean accept(File dir, String name) {
            return accept(new File(dir, name));
        }

        @Override
        public boolean accept(File file) {
            return file.getAbsolutePath().startsWith(storagePath);
        }
    };
    private final IOFileFilter XML_FILE_FILTER = andFileFilter(IN_STORE_FILTER,
            andFileFilter(fileFileFilter(), suffixFileFilter(".xml")));

}
