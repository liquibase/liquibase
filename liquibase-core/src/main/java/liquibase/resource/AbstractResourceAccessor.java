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
            String message = "Found " + streamList.size() + " files with the path '" + streamPath + "':" + System.lineSeparator();
            for (URI uri : streamList.getURIs()) {
                message += "    - " + uri.toString() + System.lineSeparator();
            }
            message += "  Search Path: " + System.lineSeparator();
            for (String location : Scope.getCurrentScope().getResourceAccessor().describeLocations()) {
                message += "    - " + location + System.lineSeparator();
            }
            message += "  You can limit the search path to remove duplicates with the liquibase.searchPath setting.";

            final GlobalConfiguration.DuplicateFileMode mode = GlobalConfiguration.DUPLICATE_FILE_MODE.getCurrentValue();
            final Logger log = Scope.getCurrentScope().getLog(getClass());

            if (mode == GlobalConfiguration.DuplicateFileMode.ERROR) {
                throw new IOException(message + " Or, if you KNOW these are the exact same file you can set liquibase.duplicateFileMode=WARN.");
            } else if (mode == GlobalConfiguration.DuplicateFileMode.WARN) {
                final String warnMessage = message + System.lineSeparator() +
                        "  To fail when duplicates are found, set liquibase.duplicateFileMode=ERROR" + System.lineSeparator() +
                        "  Choosing: " + streamList.getURIs().get(0);
                Scope.getCurrentScope().getUI().sendMessage(warnMessage);
                log.warning(warnMessage);

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
