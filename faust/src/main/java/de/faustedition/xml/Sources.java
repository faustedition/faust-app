package de.faustedition.xml;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import java.io.File;
import java.util.Deque;
import java.util.Iterator;

public class Sources implements Iterable<File>, Function<String, File> {

    public static final Joiner PATH_JOINER = Joiner.on("/");
    private final File base;

    public Sources(File base) {
        this.base = base;
    }

    @Override
    public Iterator<File> iterator() {
        return xmlFiles(base).iterator();
    }

    public Iterable<File> directory(String path) {
        return xmlFiles(new File(base, path));
    }

    protected Iterable<File> xmlFiles(File base) {
        return Files.fileTreeTraverser().preOrderTraversal(base).filter(XML_FILE);
    }

    public String path(File file) {
        final Deque<String> path = Lists.newLinkedList();
        while (!file.equals(base)) {
            path.push(file.getName());
            file = file.getParentFile();
        }
        Preconditions.checkArgument(file.equals(base), file + " not in source directory");
        return PATH_JOINER.join(path);
    }

    private static final Predicate<File> XML_FILE = new Predicate<File>() {
        @Override
        public boolean apply(File input) {
            return (input.isFile() && input.getName().endsWith(".xml"));
        }
    };

    @Override
    public File apply(String input) {
        return new File(base, input);
    }
}
