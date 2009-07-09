package liquibase.executor;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.exception.DatabaseException;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.CallableSqlStatement;
import liquibase.statement.SqlStatement;
import liquibase.util.StreamUtil;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LoggingExecutor extends AbstractExecutor implements WriteExecutor {

    private Writer output;
    private boolean alreadyCreatedChangeLockTable;
    private boolean alreadyCreatedChangeTable;

    public LoggingExecutor(Writer output, Database database) {
        this.output = output;
        setDatabase(database);
    }

    public boolean executesStatements() {
        return false;
    }

    public void execute(SqlStatement sql) throws DatabaseException {
        outputStatement(sql);
    }

    public int update(SqlStatement sql) throws DatabaseException {
        outputStatement(sql);

        return 0;
    }

    public void execute(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        outputStatement(sql, sqlVisitors);
    }

    public int update(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        outputStatement(sql, sqlVisitors);
        return 0;
    }

    public Map call(CallableSqlStatement csc, List declaredParameters, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        throw new DatabaseException("Do not know how to output callable statement");
    }

    public void comment(String message) throws DatabaseException {
        try {
            output.write(database.getLineComment());
            output.write(" ");
            output.write(message);
            output.write(StreamUtil.getLineSeparator());
        } catch (IOException e) {
            throw new DatabaseException(e);
        }
    }

    private void outputStatement(SqlStatement sql) throws DatabaseException {
        outputStatement(sql, new ArrayList<SqlVisitor>());
    }

    private void outputStatement(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        try {
            for (String statement : applyVisitors(sql, sqlVisitors)) {
                output.write(statement);


                if (database instanceof MSSQLDatabase) {
                    output.write(StreamUtil.getLineSeparator());
                    output.write("GO");
    //            } else if (database instanceof OracleDatabase) {
    //                output.write(StreamUtil.getLineSeparator());
    //                output.write("/");
                } else {
                    if (!statement.endsWith(";")) {
                        output.write(";");
                    }
                }
                output.write(StreamUtil.getLineSeparator());
                output.write(StreamUtil.getLineSeparator());
            }
        } catch (IOException e) {
            throw new DatabaseException(e);
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
