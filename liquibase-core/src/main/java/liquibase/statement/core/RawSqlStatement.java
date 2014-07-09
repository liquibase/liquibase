package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;

/**
 * Executes a SQL command. For non-SQL commands, use {@link liquibase.statement.core.RawDatabaseCommandStatement}
 */
public class RawSqlStatement extends AbstractStatement {

    public static final String SQL = "sql";
    public static final String END_DELIMITER = "endDelimiter";


    public RawSqlStatement() {
    }

    public RawSqlStatement(String sql) {
        setSql(sql);
    }

    public RawSqlStatement(String sql, String endDelimiter) {
        this(sql);
        if (endDelimiter != null) {
            setEndDelimiter(endDelimiter);
        }
    }

    public String getSql() {
        return getAttribute(SQL, String.class);
    }

    public RawSqlStatement setSql(String sql) {
        return (RawSqlStatement) setAttribute(SQL, sql);
    }

    /**
     * Returns end delimiter. Will convert "\\r" and "\\n" strings to \r and \n"
     */
    public String getEndDelimiter() {
        return getAttribute(END_DELIMITER, ";").replace("\\r","\r").replace("\\n","\n");
    }

    public RawSqlStatement setEndDelimiter(String endDelimiter) {
        return (RawSqlStatement) setAttribute(END_DELIMITER, endDelimiter);
    }

    @Override
    public String toString() {
        return getSql();
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }
}
