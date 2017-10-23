package liquibase.changelog.column;

import liquibase.structure.core.Column;

/**
 * This class is just a marker class that is used to distinguish columns that are
 * used in the DATABASECHANGELOG and DATABASECHANGELOGLOCK tables from other columns 
 * that might be used in Liquibase-managed database schemas. 
 */
public final class LiquibaseColumn extends Column {
    // no behavioral changes, just extends Column
}
