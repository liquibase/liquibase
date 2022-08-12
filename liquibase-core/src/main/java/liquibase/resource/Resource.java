package liquibase.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

public interface Resource {

    /**
     * Returns the normalized, ResourceAccessor-relative path for this resource.
     * To get the unique location of this resource, use {@link #getUri()}
     * This should always use `/` for separators
     * This should not include any sort of protocol or prefixes
     * This should not have a leading /.
     * This should have any relative paths smoothed out -- return "path/to/resource" not "path/from/../to/resource".
     */
    String getPath();


    /**
     * Opens an input stream to read from this resource.
     * @throws IOException if there is an error reading from the resource, including if the resource does not exist or cannot be read.
     */
    InputStream openInputStream() throws IOException;

    /**
     * Return true if the resource can be written to
     */
    boolean isWritable();

    /**
     * Opens an input stream to write to this resource.
     * @throws IOException if there is an error writing to the resource, including if the resource does not exist or permission don't allow writing.
     */
    OutputStream openOutputStream() throws IOException;

    /**
     * Returns a unique and complete identifier for this resource.
     * This will be different than what is returned by {@link #getPath()} because the path within the resource accessor whereas this is the a complete path to it.
     * <p>
     * For example, a file resource may return a path of <code>my/file.txt</code> and a uri of <code>file:/tmp/project/liquibase/my/file.txt</code> for a resource accessor using <code>file:/tmp/project/liquibase</code> as a root
     */
    URI getUri();

    String getAbsolutePath();
}
