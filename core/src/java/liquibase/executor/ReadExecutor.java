package liquibase.executor;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.SqlStatement;

import java.util.List;
import java.util.Map;

public interface ReadExecutor {

    void setDatabase(Database database);
    
    Object queryForObject(SqlStatement sql, Class requiredType) throws DatabaseException;

    Object queryForObject(SqlStatement sql, Class requiredType, List<SqlVisitor> sqlVisitors) throws DatabaseException;

    long queryForLong(SqlStatement sql) throws DatabaseException;

    long queryForLong(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException;

    int queryForInt(SqlStatement sql) throws DatabaseException;

    int queryForInt(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException;

    List queryForList(SqlStatement sql, Class elementType) throws DatabaseException;

    List queryForList(SqlStatement sql, Class elementType, List<SqlVisitor> sqlVisitors) throws DatabaseException;

    List<Map> queryForList(SqlStatement sql) throws DatabaseException;

    List<Map> queryForList(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException;
    
}
