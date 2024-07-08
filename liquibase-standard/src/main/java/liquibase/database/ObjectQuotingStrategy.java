package liquibase.database;

/**
 * Strategy regards quoting object names e.g. table, column, index names etc.
 */
public enum ObjectQuotingStrategy {
    LEGACY, // same behavior as in liquibase 2.0, currently only postgresql has quoting handling
    QUOTE_ALL_OBJECTS, // every object gets quoted e.g. person becomes "person"
    QUOTE_ONLY_RESERVED_WORDS // quote reserved keywords and invalid column names

}
