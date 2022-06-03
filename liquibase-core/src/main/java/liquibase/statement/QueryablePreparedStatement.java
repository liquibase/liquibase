package liquibase.statement;

import liquibase.database.PreparedStatementFactory;
import liquibase.exception.DatabaseException;
import liquibase.executor.jvm.ResultSetExtractor;
import liquibase.sql.visitor.SqlVisitor;

import java.util.List;

public interface QueryablePreparedStatement extends SqlStatement {

    Object query(PreparedStatementFactory factory, ResultSetExtractor rse, final List<SqlVisitor> sqlVisitors) throws DatabaseException;

}
