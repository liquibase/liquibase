package liquibase.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.function.Consumer;

/**
 * A list of {@link InputStream}s. Custom class to allow try-with-resources using output from {@link ResourceAccessor#openStreams(String)}.
 */
public class InputStreamList implements Iterable<InputStream>, AutoCloseable {

    private LinkedHashMap<URI, InputStream> streams = new LinkedHashMap<>();
    private Logger log = LoggerFactory.getLogger(getClass());

    public InputStreamList() {
    }

    public InputStreamList(URI uri, InputStream stream) {
        this.streams.put(uri, stream);
    }

    public boolean add(URI uri, InputStream inputStream) {
        boolean duplicate = streams.put(uri, inputStream) != null;
        if (duplicate) {
            log.debug("Closing duplicate stream for "+uri);
            try {
                inputStream.close();
            } catch (IOException e) {
                log.warn("Cannot close stream for "+uri, e);
            }
        }
        return duplicate;
    }

    public void addAll(InputStreamList streams) {
        if (streams == null) {
            return;
        }
        this.streams.putAll(streams.streams);
    }

    /**
     * Close the streams in this collection.
     */
    @Override
    public void close() throws IOException {
        for (InputStream stream : this) {
            try {
                stream.close();
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).error("Error closing stream. Logging error and continuing", e);
            }
        }
    }

    @Override
    public Iterator<InputStream> iterator() {
        return streams.values().iterator();
    }

    @Override
    public void forEach(Consumer<? super InputStream> action) {
        streams.values().forEach(action);
    }

    @Override
    public Spliterator<InputStream> spliterator() {
        return streams.values().spliterator();
    }

    public int size() {
        return streams.size();
    }

    public boolean isEmpty() {
        return streams.isEmpty();
    }
}
