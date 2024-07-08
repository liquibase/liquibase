package liquibase.changelog;

import liquibase.GlobalConfiguration;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.StreamUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class PropertyExpandingStream extends InputStream {

    private InputStream stream;

    /**
     * This method will read the content of the given stream and make any parameter update on the content. For example,
     * making a property replacement into procedure text read from the sql file.
     * @param stream
     * @return an updated {@link InputStream} if any replacement has been performed in the content of the original stream.
     */
    public PropertyExpandingStream(ChangeSet changeSet, InputStream stream) {
        try {
            if (changeSet == null) {
                this.stream = stream;
            }
            else {
                Charset encoding = GlobalConfiguration.FILE_ENCODING.getCurrentValue();
                String streamContent = StreamUtil.readStreamAsString(stream, encoding.toString());
                ChangeLogParameters parameters = changeSet.getChangeLogParameters();
                if (parameters != null) {
                    streamContent = parameters.expandExpressions(streamContent, changeSet.getChangeLog());
                }
                this.stream = new ByteArrayInputStream(streamContent.getBytes(encoding));
            }
        }
        catch (IOException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    @Override
    public int read() throws IOException {
        return this.stream.read();
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

    @Override
    public void close() throws IOException {
        stream.close();
    }
}
