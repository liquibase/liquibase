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
     * @return true if the resource defined by this object currently exists.
     */
    boolean exists();

    /**
     * Resolve the given path against this resource.
     * If other is an empty path then this method trivially returns this path.
     * Otherwise this method considers this resource to be a directory and resolves the given path against this resource.
     * <b>Even if "other" begins with a `/`, the returned resource should be relative to this resource.</b>
     */
    Resource resolve(String other);

    /**
     * Resolves the given path against this resource's parent path.
     * This is useful where a file name needs to be replaced with another file name. For example, suppose that the name separator is "/" and a path represents "dir1/dir2/foo", then invoking this method with the Path "bar" will result in the Path "dir1/dir2/bar".
     * If other is an empty path then this method returns this path's parent.
     * <b>Even if "other" begins with a `/`, the returned resource should be relative to this resource.</b>
     */
    Resource resolveSibling(String other);

    /**
     * Opens an output stream given the passed {@link OpenOptions}.
     * Cannot pass a null OpenOptions value
     */
    OutputStream openOutputStream(OpenOptions openOptions) throws IOException;

    /**
     * Opens an output stream to write to this resource using the default {@link OpenOptions#OpenOptions()} settings plus the passed createIfNeeded value.
     *
     * @deprecated use {@link #openOutputStream(OpenOptions)}
     */
    @Deprecated
    default OutputStream openOutputStream(boolean createIfNeeded) throws IOException {
        return openOutputStream(new OpenOptions().setCreateIfNeeded(createIfNeeded));
    }

    /**
     * Returns a unique and complete identifier for this resource.
     * This will be different than what is returned by {@link #getPath()} because the path within the resource accessor whereas this is the a complete path to it.
     * <p>
     * For example, a file resource may return a path of <code>my/file.txt</code> and a uri of <code>file:/tmp/project/liquibase/my/file.txt</code> for a resource accessor using <code>file:/tmp/project/liquibase</code> as a root
     */
    URI getUri();
}
