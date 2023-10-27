package liquibase.database;

import liquibase.exception.DatabaseException;

import java.util.List;

public interface LiquibaseTableNames {

    /**
     * Given a database, return a list of Liquibase generated tables. This is commonly (but not limited to) tables
     * like DATABASECHANGELOG and DATABASECHANGELOGLOCK.
     */
    List<String> getLiquibaseGeneratedTableNames(Database database);

    void destroy(Database database) throws DatabaseException;
}
