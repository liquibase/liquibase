package liquibase.resource;

import liquibase.Scope;
import liquibase.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.function.Consumer;

/**
 * A list of {@link InputStream}s. Custom class to allow try-with-resources using output from {@link ResourceAccessor#openStreams(String, String)}.
 */
public class InputStreamList implements Iterable<InputStream>, AutoCloseable {

    private LinkedHashMap<URI, InputStream> streams = new LinkedHashMap<>();

    public InputStreamList() {
    }

    public InputStreamList(URI uri, InputStream stream) {
        this.streams.put(uri, stream);
    }

    public boolean add(URI uri, InputStream inputStream) {
        Logger log = Scope.getCurrentScope().getLog(getClass());

        boolean duplicate = alreadySaw(uri);
        if (duplicate) {
            log.fine("Closing duplicate stream for " + uri);
            try {
                inputStream.close();
            } catch (IOException e) {
                log.warning("Cannot close stream for " + uri, e);
            }
        } else {
            streams.put(uri, inputStream);
        }
        return duplicate;
    }

    protected boolean alreadySaw(URI uri) {
        if (streams.isEmpty()) {
            return false;
        }
        if (streams.containsKey(uri)) {
            return true;
        }


        //standardize url strings between file:// and jar:file:
        String thisUriStringBase = uri.toString()
                .replaceFirst("^file://", "")
                .replaceFirst("^jar:file:", "")
                .replaceFirst("!/", "!");

        for (URI seenURI : streams.keySet()) {
            if (seenURI.toString()
                    .replaceFirst("^file://", "")
                    .replaceFirst("^jar:file:", "")
                    .replaceFirst("!/", "!")
                    .equals(thisUriStringBase)) {
                return true;
            }
        }
        return false;
    }

    public void addAll(InputStreamList streams) {
        if (streams == null) {
            return;
        }

        for (Map.Entry<URI, InputStream> entry : streams.streams.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
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

    public List<URI> getURIs() {
        return new ArrayList<>(streams.keySet());
    }
}
