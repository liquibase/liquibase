package liquibase.database.template;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import liquibase.database.*;
import liquibase.database.sql.SqlStatement;
import liquibase.database.sql.CallableSqlStatement;
import liquibase.database.sql.visitor.SqlVisitor;
import liquibase.exception.JDBCException;
import liquibase.util.StreamUtil;

public class JdbcOutputTemplate extends JdbcTemplate {

    private Writer output;
    private boolean alreadyCreatedChangeLockTable;
    private boolean alreadyCreatedChangeTable;

    public JdbcOutputTemplate(Writer output, Database database) {
        super(database);
        this.output = output;
    }

    public boolean executesStatements() {
        return false;
    }

    public void execute(SqlStatement sql) throws JDBCException {
        outputStatement(sql);
    }

    public int update(SqlStatement sql) throws JDBCException {
        outputStatement(sql);

        return 0;
    }

    @Override
    public Object execute(StatementCallback action, List<SqlVisitor> sqlVisitors) throws JDBCException {
        outputStatement(action.getStatement(), sqlVisitors);
        return null;
    }

    @Override
    public void execute(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws JDBCException {
        outputStatement(sql, sqlVisitors);
    }

    @Override
    public int update(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws JDBCException {
        outputStatement(sql, sqlVisitors);
        return 0;
    }

    @Override
    public Object execute(CallableSqlStatement csc, CallableStatementCallback action, List<SqlVisitor> sqlVisitors) throws JDBCException {
        throw new JDBCException("Do not know how to output callable statement");
    }

    @Override
    public Map call(CallableSqlStatement csc, List declaredParameters, List<SqlVisitor> sqlVisitors) throws JDBCException {
        throw new JDBCException("Do not know how to output callable statement");
    }

    public void comment(String message) throws JDBCException {
        try {
            output.write(database.getLineComment());
            output.write(" ");
            output.write(message);
            output.write(StreamUtil.getLineSeparator());
        } catch (IOException e) {
            throw new JDBCException(e);
        }
    }

    private void outputStatement(SqlStatement sql) throws JDBCException {

    }

    private void outputStatement(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws JDBCException {
        try {
            String statement = applyVisitors(sql, sqlVisitors);
            output.write(statement);

            if (!statement.endsWith(";")) {
              output.write(";");
            }

            if (database instanceof MSSQLDatabase) {
                output.write(StreamUtil.getLineSeparator());
                output.write("GO");
//            } else if (database instanceof OracleDatabase) {
//                output.write(StreamUtil.getLineSeparator());
//                output.write("/");
            }
            output.write(StreamUtil.getLineSeparator());
            output.write(StreamUtil.getLineSeparator());
        } catch (IOException e) {
            throw new JDBCException(e);
        }
    }

    public boolean alreadyCreatedChangeLockTable() {
        return alreadyCreatedChangeLockTable;
    }

    public void setAlreadyCreatedChangeLockTable(boolean alreadyCreatedChangeLockTable) {
        this.alreadyCreatedChangeLockTable = alreadyCreatedChangeLockTable;
    }

    public boolean alreadyCreatedChangeTable() {
        return alreadyCreatedChangeTable;
    }

    public void setAlreadyCreatedChangeTable(boolean alreadyCreatedChangeTable) {
        this.alreadyCreatedChangeTable = alreadyCreatedChangeTable;
    }
}
