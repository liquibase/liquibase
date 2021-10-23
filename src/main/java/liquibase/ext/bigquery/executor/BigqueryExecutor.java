package liquibase.ext.bigquery.executor;

import liquibase.database.Database;
import liquibase.executor.jvm.JdbcExecutor;
import liquibase.ext.bigquery.database.BigqueryDatabase;

import static liquibase.ext.bigquery.database.BigqueryDatabase.BIGQUERY_PRIORITY_DATABASE;

public class BigqueryExecutor extends JdbcExecutor {

    @Override
    public int getPriority() {
        return BIGQUERY_PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(Database database) {
        return database instanceof BigqueryDatabase;
    }

//    @Override
//    public void execute(final SqlStatement sql, final List<SqlVisitor> sqlVisitors) throws DatabaseException {
//        Scope.getCurrentScope().getLog(this.getClass()).info(String.format("Executing %s  sqlVisitors=%s", sql, sqlVisitors));
//
//        DatabaseConnection con = database.getConnection();
//        Statement stmt = ((JdbcConnection) con).getUnderlyingConnection().createStatement();
//
//        super.execute(sql,sqlVisitors);
//    }

}
