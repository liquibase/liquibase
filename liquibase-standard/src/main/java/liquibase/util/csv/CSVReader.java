package liquibase.util.csv;

import com.opencsv.CSVReaderBuilder;
import com.opencsv.RFC4180ParserBuilder;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.Reader;

public class CSVReader implements AutoCloseable {

    private final com.opencsv.CSVReader delegate;

    public static final char DEFAULT_SEPARATOR = ',';

    public static final char DEFAULT_QUOTE_CHARACTER = '"';

    public CSVReader(Reader reader) {
        this(reader, DEFAULT_SEPARATOR, DEFAULT_QUOTE_CHARACTER);
    }


    public CSVReader(Reader reader, char separator, char quotchar) {
        delegate = new CSVReaderBuilder(reader).withCSVParser(
                new RFC4180ParserBuilder().withSeparator(separator).withQuoteChar(quotchar).build()
        ).build();
    }

    @Override
    public void close() throws Exception {
        delegate.close();
    }

    public String[] readNext() throws IOException {
        try {
            return delegate.readNext();
        } catch (CsvValidationException e) {
            throw new IOException(e.getMessage(), e);
        }
    }
}
