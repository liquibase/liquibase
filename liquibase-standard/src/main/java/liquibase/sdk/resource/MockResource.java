package liquibase.sdk.resource;

import liquibase.resource.AbstractResource;
import liquibase.resource.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class MockResource extends AbstractResource {

    private final String content;

    public MockResource(String path, String content) {
        super(path, URI.create("mock:" + path));
        this.content = content;
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public Resource resolve(String other) {
        return new MockResource(resolvePath(other), "Resource relative to " + getPath());
    }

    @Override
    public Resource resolveSibling(String other) {
        return new MockResource(resolveSiblingPath(other), "Sibling resource to " + getPath());
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return new ByteArrayInputStream(content.getBytes());
    }
}
