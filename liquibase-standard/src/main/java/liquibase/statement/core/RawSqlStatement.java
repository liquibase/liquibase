package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import lombok.Getter;

/**
 * @deprecated use {@link RawParameterizedSqlStatement}
 */
@Deprecated
public class RawSqlStatement extends AbstractSqlStatement {

    @Getter
    private final String sql;
    private String endDelimiter  = ";";


    public RawSqlStatement(String sql) {
        this.sql = sql;
    }

    public RawSqlStatement(String sql, String endDelimiter) {
        this(sql);
        if (endDelimiter != null) {
            this.endDelimiter = endDelimiter;
        }
    }

    public String getEndDelimiter() {
        return endDelimiter.replace("\\r","\r").replace("\\n","\n");
    }

    @Override
    public String toString() {
        return sql;
    }
}
