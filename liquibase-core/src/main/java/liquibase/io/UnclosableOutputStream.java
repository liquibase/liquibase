package liquibase.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class is a wrapper around OutputStreams, and makes them impossible for callers to close.
 */
public class UnclosableOutputStream extends FilterOutputStream {
    public UnclosableOutputStream(OutputStream out) {
        super(out);
    }

    /**
     * This method does not actually close the underlying stream, but rather only flushes it. Callers should not be
     * closing the stream they are given.
     */
    @Override
    public void close() throws IOException {
        out.flush();
    }
}
