package liquibase.util.csv;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;

public class CSVWriter implements AutoCloseable, Flushable {
    private final ICSVWriter delegate;

    public CSVWriter(Writer writer) {
        delegate = new CSVWriterBuilder(writer).build();
    }

    @Override
    public void close() throws Exception {
        delegate.close();
    }

    public void writeNext(String[] nextLine) {
        delegate.writeNext(nextLine);
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }
}
