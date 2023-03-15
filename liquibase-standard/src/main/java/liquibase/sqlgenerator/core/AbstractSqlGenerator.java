package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.Warnings;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.SqlStatement;

/**
 * Generic template class for an SQL generator able to generate SQL for an object-form
 * {@link liquibase.statement.SqlStatement}.
 *
 * @param <T> an implementation of the {@link liquibase.statement.SqlStatement} specifying which
 *            logical SQL statement an instance of this template class is able to generate SQL for.
 */
public abstract class AbstractSqlGenerator<T extends SqlStatement> implements SqlGenerator<T> {

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public boolean generateStatementsIsVolatile(Database database) {
        return false;
    }

    @Override
    public boolean generateRollbackStatementsIsVolatile(Database database) {
        return false;
    }

    @Override
    public boolean supports(T statement, Database database) {
        return true;
    }

    @Override
    public Warnings warn(T statementType, Database database, SqlGeneratorChain<T> sqlGeneratorChain) {
        return sqlGeneratorChain.warn(statementType, database);
    }

    /**
     * Tries to find out if a given value (part a database-specific SQL statement) is just a simple literal
     * (e.g. 'Jones', 149.99 or false) or if it is a call to a function within the database (e.g.
     * TO_DATE('28.12.2017', 'dd.mm.yyyy') in Oracle DB).
     * This method is often used to determine if we need to quote literals which are Strings, e.g. if we determine
     * "Dr. Jones" to be a literal, then we generate
     * <pre><code>{@code INSERT INTO customers(last_name) VALUES('Dr. Jones')}</code></pre>
     * but if the value is a function call, we may not quote it, e.g. for "TO_DATE('28.12.2017', 'dd.mm.yyyy')", we
     * need to generate (note the absence of the apostrophes!):
     * <pre><code>{@code INSERT INTO bookings(booking_date) VALUES (TO_DATE('28.12.2017', 'dd.mm.yyyy'))}</code></pre>
     * @param value The string to test
     * @param database the database object to test against
     * @return true if value looks like a function call, false if it looks like a literal.
     */
    public boolean looksLikeFunctionCall(String value, Database database) {
        // TODO: SYSIBM looks DB2-specific, we should move that out of AbstractSqlGenerator into a DB2-specific class.
        return value.startsWith("\"SYSIBM\"") || value.startsWith("to_date(") ||
            value.equalsIgnoreCase(database.getCurrentDateTimeFunction());
    }

}
