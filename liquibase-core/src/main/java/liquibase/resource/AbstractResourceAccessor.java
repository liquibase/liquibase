package liquibase.resource;

import liquibase.AbstractExtensibleObject;
import liquibase.Scope;
import liquibase.util.StringUtil;

import java.io.*;
import java.util.SortedSet;

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
            Scope.getCurrentScope().getLog(getClass()).warning("ResourceAccessor roots: " + Scope.getCurrentScope().getResourceAccessor().getClass().getName());
            throw new IOException("Found " + streamList.size() + " files that match " + streamPath + ": " + StringUtil.join(streamList.getURIs(), ", ", new StringUtil.ToStringFormatter()));
        } else {
            return streamList.iterator().next();
        }
    }

    /**
     * Open the given path for writing. If the file path cannot be written to, return null.
     */
    @Override
    public OutputStream openOutputStream(String relativeTo, String path, boolean append) throws IOException {
        File outputFile = getOutputFile(relativeTo, path);
        if (outputFile == null) {
            return null;
        }

        if (!outputFile.getParentFile().mkdirs()) {
            throw new IOException("Cannot create directory " + outputFile.getParentFile().getAbsolutePath());
        }

        Scope.getCurrentScope().getLog(getClass()).fine("Opening file " + outputFile.getAbsolutePath() + " for writing");
        return new FileOutputStream(outputFile, append);
    }

    protected abstract File getOutputFile(String relativeTo, String path);

    @Override
    public boolean exists(String relativeTo, String path) {
        try {
            final SortedSet<String> list = list(relativeTo, path, false, true, true);

            return list.size() > 0;
        } catch (IOException e) {
            Scope.getCurrentScope().getLog(getClass()).fine("Cannot check existence of " + path + " relative to " + relativeTo + ": " + e.getMessage(), e);
            return false;
        }
    }
}
