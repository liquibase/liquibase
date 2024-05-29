package liquibase.util;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.core.MSSQLDatabase;
import liquibase.exception.DatabaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.structure.core.Catalog;

import java.text.DateFormat;
import java.util.Date;

/**
 * Write texts to the current logging executor
 */
public class LoggingExecutorTextUtil {

    private LoggingExecutorTextUtil() {}

    public static void outputHeader(String message, Database database, String changeLogFile) throws DatabaseException {
        Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("logging", database);
        executor.comment("*********************************************************************");
        executor.comment(message);
        executor.comment("*********************************************************************");
        executor.comment("Change Log: " + changeLogFile);
        executor.comment("Ran at: " +
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date())
        );
        DatabaseConnection connection = database.getConnection();
        if (connection != null) {
            executor.comment("Against: " + connection.getConnectionUserName() + "@" + connection.getURL());
        }
        executor.comment("Liquibase version: " + LiquibaseUtil.getBuildVersionInfo());
        executor.comment("*********************************************************************" +
                StreamUtil.getLineSeparator()
        );

        if ((database instanceof MSSQLDatabase) && (database.getDefaultCatalogName() != null)) {
            executor.execute(new RawParameterizedSqlStatement(String.format("USE %s;",
                    database.escapeObjectName(database.getDefaultCatalogName(), Catalog.class))
            ));
        }
    }
}
