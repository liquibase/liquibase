package liquibase.executor;

import liquibase.statement.SqlStatement;
import liquibase.exception.JDBCException;
import liquibase.sql.visitor.SqlVisitor;

import java.util.List;
import java.util.Map;

public interface ReadExecutor {
        List query(SqlStatement sql, RowMapper rowMapper) throws JDBCException;

    List query(SqlStatement sql, RowMapper rowMapper, List<SqlVisitor> sqlVisitors) throws JDBCException;

    Object queryForObject(SqlStatement sql, RowMapper rowMapper) throws JDBCException;

    Object queryForObject(SqlStatement sql, RowMapper rowMapper, List<SqlVisitor> sqlVisitors) throws JDBCException;

    Object queryForObject(SqlStatement sql, Class requiredType) throws JDBCException;

    Object queryForObject(SqlStatement sql, Class requiredType, List<SqlVisitor> sqlVisitors) throws JDBCException;

    long queryForLong(SqlStatement sql) throws JDBCException;

    long queryForLong(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws JDBCException;

    int queryForInt(SqlStatement sql) throws JDBCException;

    int queryForInt(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws JDBCException;

    List queryForList(SqlStatement sql, Class elementType) throws JDBCException;

    List queryForList(SqlStatement sql, Class elementType, List<SqlVisitor> sqlVisitors) throws JDBCException;

    List<Map> queryForList(SqlStatement sql) throws JDBCException;

    List<Map> queryForList(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws JDBCException;

    Object query(SqlStatement sql, ResultSetExtractor rse) throws JDBCException;

    Object query(SqlStatement sql, ResultSetExtractor rse, List<SqlVisitor> sqlVisitors) throws JDBCException;
    
}
