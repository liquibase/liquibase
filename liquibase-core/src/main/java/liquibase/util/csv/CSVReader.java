package liquibase.util.csv;

import java.io.Reader;

public class CSVReader extends liquibase.util.csv.opencsv.CSVReader {

    public static final char DEFAULT_SEPARATOR = ',';

    public static final char DEFAULT_QUOTE_CHARACTER = '"';


    public CSVReader(Reader reader) {
        super(reader);
    }

    public CSVReader(Reader reader, char c) {
        super(reader, c);
    }

    public CSVReader(Reader reader, char c, char c1) {
        super(reader, c, c1);
    }

    public CSVReader(Reader reader, char c, char c1, int i) {
        super(reader, c, c1, i);
    }
}
