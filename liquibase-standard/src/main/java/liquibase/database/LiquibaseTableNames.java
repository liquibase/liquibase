package liquibase.database;

import java.util.List;

public interface LiquibaseTableNames {

    /**
     * Given a database, return a list of Liquibase generated tables. This is commonly (but not limited to) tables
     * like DATABASECHANGELOG and DATABASECHANGELOGLOCK.
     */
    List<String> getLiquibaseGeneratedTableNames(Database database);
}
