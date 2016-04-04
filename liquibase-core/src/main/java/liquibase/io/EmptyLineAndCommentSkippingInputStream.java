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
    public static final int MAX_NEW_LINE_CHARS = 2;
    private final String commentLineStartsWith;
    private final boolean commentSkipEnabled;
    private final int commentLineStartsWithLength;

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
        this.commentLineStartsWithLength = commentSkipEnabled ? commentLineStartsWith.length() : -1;
    }

    @Override
    public synchronized int read() throws IOException {
        if (isPositionAtStart() || skipIfNewLine()) {
            skipCommentsAndNewLines();
        }

        return super.read();
    }

    private boolean isPositionAtStart() {
        return pos == 0;
    }

    /**
     * Skips commented lines and line endings
     *
     * @throws IOException
     */
    private void skipCommentsAndNewLines() throws IOException {
        while (skipLineIfItsCommented() || skipIfNewLine()) ;
    }

    private boolean skipIfNewLine() throws IOException {
        mark(MAX_CHAR_SIZE_IN_BYTES);//create marker to be able to return

        int char1 = super.read();
        if (char1 == '\r' || char1 == '\n') {
            mark(MAX_CHAR_SIZE_IN_BYTES);//its a new line, so lets create a new marker, it may be \r\n
        } else {
            reset();
            return false;
        }

        int char2 = super.read();
        if (char2 == '\n') {
            return true;//it was \r\n - windows style
        } else {
            reset(); //it was either \r or \n, so we need to reset position just after \r or \n
            return true;
        }

    }

    private boolean skipLineIfItsCommented() throws IOException {
        if (!commentSkipEnabled) {
            return false;
        }

        mark(commentLineStartsWithLength * MAX_CHAR_SIZE_IN_BYTES); //mark current position to reset it in case this is not a comment

        for (int i = 0; i < commentLineStartsWithLength; i++) {
            if (commentLineStartsWith.charAt(i) != super.read()) {
                reset(); //this is not a comment, move cursor to marker

                return false;
            }
        }

        readUntilEndOfLine(); //its a comment - consume whole line
        return true;
    }

    private void readUntilEndOfLine() throws IOException {
        while (!skipIfNewLine()) {
            super.read();
        }
    }


}
