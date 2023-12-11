package liquibase.listener;

/**
 * Liquibase listener for executed SQL
 */
public class SqlListener implements LiquibaseListener {

    /**
     * Called for any "query" sql that reads information from the database without writing to the database.
     */
    @SuppressWarnings("unused")
    public void readSqlWillRun(String sql) {

    }

    /**
     * Called for any sql that modifies data, schema structure, or anything else in the database.
     */
    @SuppressWarnings("unused")
    public void writeSqlWillRun(String sql) {

    }
}
