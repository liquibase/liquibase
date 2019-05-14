package liquibase.resource;

import liquibase.Scope;
import liquibase.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A list of {@link InputStream}s. Custom class to allow try-with-resources using output from {@link ResourceAccessor#openStreams(String, String)}.
 */
public class InputStreamList implements Iterable<InputStream>, AutoCloseable {

    private LinkedHashMap<URI, InputStreamSupplier> streams = new LinkedHashMap<>();

    public InputStreamList() {
    }

    /*
    public InputStreamList(URI uri, InputStream stream) {
        this.streams.put(uri, stream);
    }*/

    public InputStreamList(URI uri, InputStreamSupplier stream) {
        this.streams.put(uri, stream);
    }

    public boolean add(URI uri, InputStreamSupplier inputStream) {
        return streams.put(uri, inputStream) != null;
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
                Scope.getCurrentScope().getLog(getClass()).severe("Error closing stream. Logging error and continuing", e);
            }
        }
    }

    @Override
    public Iterator<InputStream> iterator() {
        return streams.values().stream().map(InputStreamSupplier::get).iterator();
    }

    @Override
    public void forEach(Consumer<? super InputStream> action) {
        streams.values().stream().map(InputStreamSupplier::get).forEach(action);
    }

    @Override
    public Spliterator<InputStream> spliterator() {
        return streams.values().stream().map(InputStreamSupplier::get).spliterator();
    }

    public int size() {
        return streams.size();
    }

    public boolean isEmpty() {
        return streams.isEmpty();
    }

    public List<URI> getURIs() {
        return new ArrayList<>(streams.keySet());
    }
}
