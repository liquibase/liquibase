package liquibase.resource;

import liquibase.AbstractExtensibleObject;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.Logger;
import liquibase.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;

/**
 * Convenience base class for {@link ResourceAccessor} implementations.
 */
public abstract class AbstractResourceAccessor extends AbstractExtensibleObject implements ResourceAccessor {

    @Override
    @java.lang.SuppressWarnings("squid:S2095")
    public InputStream openStream(String relativeTo, String streamPath) throws IOException {
        InputStreamList streamList = this.openStreams(relativeTo, streamPath);

        if (streamList == null || streamList.size() == 0) {
            return null;
        } else if (streamList.size() > 1) {
            String message = "Found " + streamList.size() + " files matching '" + streamPath + "':" + System.lineSeparator();
            for (URI uri : streamList.getURIs()) {
                message += "    - " + uri.toString() + System.lineSeparator();
            }
            message += "  Search Path: " + System.lineSeparator();
            for (String location : Scope.getCurrentScope().getResourceAccessor().describeLocations()) {
                message += "    - " + location + System.lineSeparator();
            }

            final GlobalConfiguration.DuplicateFileMode mode = GlobalConfiguration.DUPLICATE_FILE_MODE.getCurrentValue();
            final Logger log = Scope.getCurrentScope().getLog(getClass());

            if (mode == GlobalConfiguration.DuplicateFileMode.ERROR) {
                throw new IOException(message);
            } else if (mode == GlobalConfiguration.DuplicateFileMode.WARN) {
                log.warning(message + "  Using " + streamList.getURIs().get(0));

                InputStream returnStream = null;
                final Iterator<InputStream> iterator = streamList.iterator();
                while (iterator.hasNext()) {
                    if (returnStream == null) {
                        returnStream = iterator.next();
                    } else {
                        iterator.next().close();
                    }
                }
                return returnStream;
            } else {
                throw new UnexpectedLiquibaseException("Unexpected DuplicateFileMode: " + mode);
            }
        } else {
            return streamList.iterator().next();
        }
    }
}
