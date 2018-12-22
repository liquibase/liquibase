package liquibase.resource;

import liquibase.AbstractExtensibleObject;
import liquibase.exception.LiquibaseException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Convenience base class for {@link ResourceAccessor} implementations.
 */
public abstract class AbstractResourceAccessor extends AbstractExtensibleObject implements ResourceAccessor {

    @Override
    public InputStream openStream(String path) throws IOException {
        InputStreamList streamList = this.openStreams(path);

        if (streamList == null || streamList.size() == 0) {
            return null;
        } else if (streamList.size() > 1) {
            streamList.close();
            throw new IOException("Found " + streamList.size() + " files that match " + path);
        } else {
            return streamList.iterator().next();
        }
    }
}
