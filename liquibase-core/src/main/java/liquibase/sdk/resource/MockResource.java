package liquibase.sdk.resource;

import liquibase.resource.AbstractResource;

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
    public InputStream openInputStream() throws IOException {
        return new ByteArrayInputStream(content.getBytes());
    }

    @Override
    public String getAbsolutePath() {
        return null;
    }
}
