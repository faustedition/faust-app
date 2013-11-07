package de.faustedition.index;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import com.google.common.io.Closer;
import com.google.common.util.concurrent.AbstractScheduledService;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Index extends AbstractScheduledService {
    public static final Version LUCENE_VERSION = Version.LUCENE_36;

    private static final Logger LOG = Logger.getLogger(Index.class.getName());

    private final File dataDirectory;

    private Directory directory;
    private SearcherManager searchManager;


    public static QueryParser queryParser(String defaultField) {
        return new QueryParser(LUCENE_VERSION, defaultField, analyzer());
    }

    public Index(File dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public <T> T transaction(TransactionCallback<T> cb) throws Exception {
        Stopwatch sw = null;
        try {
            if (LOG.isLoggable(Level.FINE)) {
                sw = Stopwatch.createStarted();
            }

            cb.index = this;

            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Started transaction for {0}", cb);
            }

            final T result = cb.doInTransaction();

            cb.commit();
            if (LOG.isLoggable(Level.FINE)) {
                sw.stop();
                LOG.log(Level.FINE, "Committed transaction for {0} after {1}", new Object[] { cb , sw });
            }

            return result;
        } catch (Exception e) {
            if (cb.rollsBackOn(e)) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Rolled back transaction for " + cb, e);
                }
            } else {
                cb.commit();
            }
            throw e;
        } finally {
            cb.close();
        }
    }

    public static abstract class TransactionCallback<T> implements Closeable {

        private Index index;
        private Closer closer = Closer.create();
        private IndexReader reader;
        private IndexWriter writer;
        private IndexSearcher searcher;

        public abstract T doInTransaction() throws Exception;

        public boolean rollsBackOn(Exception e) {
            return true;
        }

        protected IndexReader reader() throws IOException {
            if (reader == null) {
                reader = closer.register(index.reader());
            }
            return reader;
        }

        protected IndexWriter writer() throws IOException {
            if (writer == null) {
                writer = closer.register(index.writer());
            }
            return writer;
        }

        protected IndexSearcher searcher() throws IOException {
            if (searcher == null) {
                searcher = index.searchManager().acquire();
            }
            return searcher;
        }

        public void commit() throws IOException {
            if (writer != null) {
                writer.commit();
            }
        }

        @Override
        public void close() throws IOException {
            try {
                if (searcher != null) {
                    index.searchManager().release(searcher);
                }
            } finally {
                closer.close();
            }
        }
    }

    public boolean isEmpty() {
        final Closer closer = Closer.create();
        try {
            return (closer.register(reader()).numDocs() == 0);
        } catch (IOException e) {
            if (LOG.isLoggable(Level.SEVERE)) {
                LOG.log(Level.SEVERE, "I/O while checking whether index is empty; assuming it is", e);
            }
            return true;
        } finally {
            try {
                closer.close();
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
        }

    }

    protected SearcherManager searchManager() throws IOException {
        synchronized (this) {
            if (searchManager == null) {
                searchManager = new SearcherManager(directory(), new SearcherFactory());
            }
        }
        return searchManager;
    }

    protected IndexWriter writer() throws IOException {
        return new IndexWriter(directory(), standardIndexWriterConfig());
    }

    protected IndexReader reader() throws IOException {
        return IndexReader.open(directory());
    }

    protected Directory directory() throws IOException {
        synchronized (this) {
            if (directory == null) {
                directory = FSDirectory.open(dataDirectory);
            }
        }
        return directory;
    }

    protected static IndexWriterConfig standardIndexWriterConfig() {
        return new IndexWriterConfig(
                LUCENE_VERSION,
                analyzer()
        ).setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND).setWriteLockTimeout(30000);
    }

    protected static Analyzer analyzer() {
        return new CustomAnalyzer();
    }

    @Override
    protected void startUp() throws Exception {
        IndexWriter indexWriter = null;
        try {
            indexWriter = writer();
            indexWriter.commit();
        } finally {
            Closeables.close(indexWriter, false);
        }
    }

    @Override
    protected void shutDown() throws Exception {
        synchronized (this) {
            searchManager.close();
        }
    }

    @Override
    protected void runOneIteration() throws Exception {
        synchronized (this) {
            if (searchManager != null) {
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.FINER, "Refreshing index search manager(s)");
                }
                try {
                    searchManager.maybeRefresh();
                } catch (IOException e) {
                }
            }
        }
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedRateSchedule(0, 60, TimeUnit.SECONDS);
    }
}
