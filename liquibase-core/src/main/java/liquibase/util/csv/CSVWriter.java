package liquibase.util.csv;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import liquibase.util.ISODateFormat;

import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;

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

    public void flush() throws IOException {
        delegate.flush();
    }
}
