package liquibase.io;

import liquibase.util.StringUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Input stream that does not read (skips) lines starting with <code>commentPattern</code> and line endings.
 * {@link #read()} method will not return either line endings or commented lines.
 */
public class EmptyLineAndCommentSkippingInputStream extends BufferedInputStream {
    public static final int MAX_CHAR_SIZE_IN_BYTES = 4;
    private String commentLineStartsWith;
    private final boolean commentSkipEnabled;

    private int lastRead = -1;

    /**
     * Creates  Input stream that does not read (skips) lines starting with <code>commentLineStartsWith</code>
     *
     * @param in                    original input stream
     * @param commentLineStartsWith comment line pattern (if empty or null, comments will not be enabled)
     */
    public EmptyLineAndCommentSkippingInputStream(InputStream in, String commentLineStartsWith) {
        super(in);

        this.commentLineStartsWith = commentLineStartsWith;
        this.commentSkipEnabled = StringUtils.isNotEmpty(commentLineStartsWith);
    }

    @Override
    public synchronized int read() throws IOException {
        return read(this.lastRead, false);
    }

    private int read(final int lastRead, final boolean lookAhead) throws IOException {
        int read = super.read();

        // skip comment
        if (commentSkipEnabled && (read == this.commentLineStartsWith.toCharArray()[0])
                && (lastRead == '\n' || lastRead < 0)) {
            while ((((read = super.read())) != '\n') && (read != '\r') && (read > 0)) {
                ;//keep looking
            }
        }

        if (read < 0) {
            return read;
        }
        if (read == '\r') {
            return this.read();
        }
        if (read == '\n') {
            if (lastRead == '\n') {
                return this.read();
            }
        }

        if (read == '\n') {
            if (lastRead < 0) {  //don't include beginning newlines
                return this.read();
            } else {//don't include last newline
                mark(MAX_CHAR_SIZE_IN_BYTES);
                if (this.read('\n', true) < 0) {
                    return -1;
                } else {
                    reset();
                }
            }
        }

        if (!lookAhead) {
            this.lastRead = read;
        }
        return read;

    }
}
