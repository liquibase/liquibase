package liquibase.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipEntryResource extends AbstractResource {
    private final ZipEntry entry;
    private final ZipFile jar;

    public ZipEntryResource(String path, ZipEntry entry, ZipFile jar) {
        super(path);
        this.entry = entry;
        this.jar = jar;
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return jar.getInputStream(entry);
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        throw new IOException("Cannot write");
    }
}
