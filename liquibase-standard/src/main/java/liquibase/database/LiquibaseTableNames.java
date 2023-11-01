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

    /**
     * Returns the order in which modifiers should be run. Modifiers with a higher order will run after modifiers with a lower order value.
     *
     * @return int
     */
    int getOrder();
}
