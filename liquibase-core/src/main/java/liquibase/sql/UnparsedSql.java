package liquibase.sql;

import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtils;

import java.util.*;

public class UnparsedSql implements Sql {

    private String sql;
    private String endDelimiter;


    public UnparsedSql(String sql, DatabaseObject... affectedDatabaseObjects) {
        this(sql, ";", affectedDatabaseObjects);
    }

    public UnparsedSql(String sql, String endDelimiter, DatabaseObject... affectedDatabaseObjects) {
        this.sql = StringUtils.trimToEmpty(sql.trim());
        this.endDelimiter = endDelimiter;
    }

    @Override
    public String toSql() {
        return sql;
    }

    @Override
    public String toString() {
        return toSql()+getEndDelimiter();
    }

    @Override
    public String getEndDelimiter() {
        return endDelimiter;
    }

}
