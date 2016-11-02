package liquibase.util.csv.opencsv.enums;

/**
 * Enumeration used to tell the CSVParser what to consider null.
 * <p/>
 * EMPTY_SEPARATORS - two sequential separators are null.
 * EMPTY_QUOTES - two sequential quotes are null
 * BOTH - both are null
 * NEITHER - default.  Both are considered empty string.
 */
public enum CSVReaderNullFieldIndicator {
    EMPTY_SEPARATORS,
    EMPTY_QUOTES,
    BOTH,
    NEITHER;
}
