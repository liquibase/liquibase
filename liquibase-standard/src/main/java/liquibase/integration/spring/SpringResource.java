package liquibase.integration.spring;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.resource.OpenOptions;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 * {@link liquibase.resource.Resource} implementation around a {@link Resource}
 */
class SpringResource extends liquibase.resource.AbstractResource {

    private final Resource resource;

    public SpringResource(String path, URI uri, Resource resource) {
        super(path, uri);
        this.resource = resource;
    }

    @Override
    public boolean exists() {
        return resource.exists();
    }

    @Override
    public liquibase.resource.Resource resolve(String other) {
        try {
            Resource otherResource = this.resource.createRelative(other);

            return new SpringResource(resolvePath(other), otherResource.getURI(), otherResource);
        } catch (IOException e) {
            throw new UnexpectedLiquibaseException(e.getMessage(), e);
        }
    }

    @Override
    public liquibase.resource.Resource resolveSibling(String other) {
        try {
            Resource otherResource = this.resource.createRelative(other);

            return new SpringResource(resolveSiblingPath(other), otherResource.getURI(), otherResource);
        } catch (IOException e) {
            throw new UnexpectedLiquibaseException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isWritable() {
        return resource instanceof WritableResource && ((WritableResource) resource).isWritable();
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return resource.getInputStream();
    }

    @Override
    public OutputStream openOutputStream(OpenOptions openOptions) throws IOException {
        if (!resource.exists() && !openOptions.isCreateIfNeeded()) {
            throw new IOException("Resource " + getUri() + " does not exist");
        }
        if (openOptions != null && openOptions.isAppend() && exists()) {
            throw new IOException("Spring only supports truncating the existing resources.");
        }
        if (resource instanceof WritableResource) {
            return ((WritableResource) resource).getOutputStream();
        }
        throw new IOException("Read only");
    }
}
