package liquibase.database;

import liquibase.exception.LiquibaseException;

import java.util.List;

public interface LiquibaseTableNames {

    /**
     * Given a database, return a list of Liquibase generated tables. This is commonly (but not limited to) tables
     * like DATABASECHANGELOG and DATABASECHANGELOGLOCK.
     */
    List<String> getLiquibaseGeneratedTableNames(Database database);

    /**
     * Drops the Liquibase-generated tables this modifier knows about.
     *
     * <p>ADR-0005 (INT-2205 phase 2): the clause is widened from {@code DatabaseException} to
     * {@code LiquibaseException} so a classified business/system failure raised deeper in the
     * teardown chain (e.g. {@link liquibase.lockservice.LockService#destroy()}) can flow through
     * unwrapped. Existing implementors may keep a narrower {@code throws DatabaseException} override
     * unchanged (source-compatible) — {@code ProLiquibaseTableNames} does exactly that.
     *
     * @throws LiquibaseException if the teardown fails
     */
    void destroy(Database database) throws LiquibaseException;

    /**
     * Returns the order in which modifiers should be run. Modifiers with a higher order will run after modifiers with a lower order value.
     *
     * @return int
     */
    int getOrder();
}
