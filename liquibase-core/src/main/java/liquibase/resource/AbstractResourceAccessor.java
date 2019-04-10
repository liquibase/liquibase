package liquibase.resource;

import liquibase.AbstractExtensibleObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * Convenience base class for {@link ResourceAccessor} implementations.
 */
public abstract class AbstractResourceAccessor extends AbstractExtensibleObject implements ResourceAccessor {

    @Override
    public InputStream openStream(String relativeTo, String streamPath) throws IOException {
        InputStreamList streamList = this.openStreams(relativeTo, streamPath);

        if (streamList == null || streamList.size() == 0) {
            return null;
        } else if (streamList.size() > 1) {
            streamList.close();
            throw new IOException("Found " + streamList.size() + " files that match " + streamPath);
        } else {
            return streamList.iterator().next();
        }
    }
}
