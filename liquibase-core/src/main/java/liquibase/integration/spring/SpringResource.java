package liquibase.integration.spring;

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
    public boolean isWritable() {
        return resource instanceof WritableResource && ((WritableResource) resource).isWritable();
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return resource.getInputStream();
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        if (resource instanceof WritableResource) {
            return ((WritableResource) resource).getOutputStream();
        }
        throw new IOException("Read only");
    }
}
