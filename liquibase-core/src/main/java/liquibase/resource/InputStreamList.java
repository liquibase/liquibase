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

    private LinkedHashMap<String, InputStream> streams = new LinkedHashMap<>();

    public InputStreamList() {
    }

    public InputStreamList(String description, InputStream stream) {
        this.streams.put(description, stream);
    }

    public boolean add(String description, InputStream inputStream) {
        Logger log = Scope.getCurrentScope().getLog(getClass());

        boolean duplicate = alreadySaw(description);
        if (duplicate) {
            log.fine("Closing duplicate stream for " + description);
            try {
                inputStream.close();
            } catch (IOException e) {
                log.warning("Cannot close stream for " + description, e);
            }
        } else {
            streams.put(description, inputStream);
        }
        return duplicate;
    }

    protected boolean alreadySaw(String description) {
        if (streams.isEmpty()) {
            return false;
        }
        if (streams.containsKey(description)) {
            return true;
        }


        //standardize url strings between file:// and jar:file:
        String thisDescriptionBase = description.toString()
                .replaceFirst("^file://", "")
                .replaceFirst("^jar:file:", "")
                .replaceFirst("!/", "!");

        for (String seenDescription : streams.keySet()) {
            if (seenDescription.toString()
                    .replaceFirst("^file://", "")
                    .replaceFirst("^jar:file:", "")
                    .replaceFirst("!/", "!")
                    .equals(thisDescriptionBase)) {
                return true;
            }
        }
        return false;
    }

    public void addAll(InputStreamList streams) {
        if (streams == null) {
            return;
        }

        for (Map.Entry<String, InputStream> entry : streams.streams.entrySet()) {
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

    public List<String> getDescriptions() {
        return new ArrayList<>(streams.keySet());
    }
}
