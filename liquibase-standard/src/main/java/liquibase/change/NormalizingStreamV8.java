package liquibase.change;

import liquibase.GlobalConfiguration;
import liquibase.exception.UnexpectedLiquibaseException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Deprecated and only to be used for V8 checksum calculation.
 */
@Deprecated
public class NormalizingStreamV8 extends InputStream {
    private ByteArrayInputStream headerStream;
    private PushbackInputStream stream;

    private byte[] quickBuffer = new byte[100];
    private List<Byte> resizingBuffer = new ArrayList<>();
    private int lastChar = 'X';
    private boolean seenNonSpace;

    @Deprecated
    public NormalizingStreamV8(String endDelimiter, Boolean splitStatements, Boolean stripComments, InputStream stream) {
        this.stream = new PushbackInputStream(stream, 2048);
        try {
            this.headerStream = new ByteArrayInputStream((endDelimiter+":"+splitStatements+":"+stripComments+":").getBytes(GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()));
        } catch (UnsupportedEncodingException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    @Override
    public int read() throws IOException {
        if (headerStream != null) {
            int returnChar = headerStream.read();
            if (returnChar != -1) {
                return returnChar;
            }
            headerStream = null;
        }

        int returnChar = stream.read();
        if (isWhiteSpace(returnChar)) {
            returnChar = ' ';
        }

        while ((returnChar == ' ') && (!seenNonSpace || (lastChar == ' '))) {
            returnChar = stream.read();

            if (isWhiteSpace(returnChar)) {
                returnChar = ' ';
            }
        }

        seenNonSpace = true;

        lastChar = returnChar;

        if ((lastChar == ' ') && isOnlyWhitespaceRemaining()) {
            return -1;
        }

        return returnChar;
    }
    @Override
    public int available() throws IOException {
        return stream.available();
    }
    @Override
    public boolean markSupported() {
        return stream.markSupported();
    }
    @Override
    public synchronized void mark(int readLimit) {
        stream.mark(readLimit);
    }
    @Override
    public synchronized void reset() throws IOException {
        stream.reset();
    }

    private boolean isOnlyWhitespaceRemaining() throws IOException {
        try {
            int quickBufferUsed = 0;
            while (true) {
                byte read = (byte) stream.read();
                if (quickBufferUsed >= quickBuffer.length) {
                    resizingBuffer.add(read);
                } else {
                    quickBuffer[quickBufferUsed++] = read;
                }

                if (read == -1) {
                    return true;
                }
                if (!isWhiteSpace(read)) {
                    if (!resizingBuffer.isEmpty()) {

                        byte[] buf = new byte[resizingBuffer.size()];
                        for (int i=0; i< resizingBuffer.size(); i++) {
                            buf[i] = resizingBuffer.get(i);
                        }

                        stream.unread(buf);
                    }

                    stream.unread(quickBuffer, 0, quickBufferUsed);
                    return false;
                }
            }
        } finally {
            resizingBuffer.clear();
        }
    }

    private boolean isWhiteSpace(int read) {
        return (read == ' ') || (read == '\n') || (read == '\r') || (read == '\t');
    }
    @Override
    public void close() throws IOException {
        stream.close();
    }
}
