package de.faustedition.db;

import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.io.filefilter.FileFilterUtils.andFileFilter;
import static org.apache.commons.io.filefilter.FileFilterUtils.fileFileFilter;
import static org.apache.commons.io.filefilter.FileFilterUtils.suffixFileFilter;
import static org.apache.commons.io.filefilter.FileFilterUtils.trueFileFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.w3c.dom.Document;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import de.faustedition.xml.XmlUtil;

@Singleton
public class XmlStorage implements Iterable<File> {

    private static final IOFileFilter XML_FILE_FILTER = andFileFilter(fileFileFilter(), suffixFileFilter(".xml"));
    private final File storageDirectory;

    @Inject
    public XmlStorage(@Named("db.home") String dbDirectory) {
        this.storageDirectory = new File(dbDirectory, "xml");
        Preconditions.checkArgument(this.storageDirectory.isDirectory(), storageDirectory.getAbsolutePath() + " is a directory");
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<File> iterator() {
        return listFiles(storageDirectory, XML_FILE_FILTER, trueFileFilter()).iterator();
    }

    public File getFile(String path) {
        final File file = new File(storageDirectory, path);
        Preconditions.checkArgument(XML_FILE_FILTER.accept(file), path + " is an XML document file");

        return file;
    }

    public Document getDocument(String path) throws IOException {
        FileInputStream stream = null;
        try {
            return XmlUtil.parse(stream = new FileInputStream(getFile(path)));
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

}
