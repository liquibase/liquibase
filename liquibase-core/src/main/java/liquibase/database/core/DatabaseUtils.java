package liquibase.database.core;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.OfflineConnection;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.core.Schema;

public class DatabaseUtils {
    /**
   * Executes RawSqlStatements particular to each database engine to set the default schema for the given Database
   *
   * @param defaultCatalogName Catalog name and schema name are similar concepts.
   *                           Used if defaultCatalogName is null.
   * @param defaultSchemaName  Catalog name and schema name are similar concepts.
   *                           Catalog is used with Oracle, DB2 and MySQL, and takes
   *                           precedence over the schema name.
   * @param database           Which Database object is affected by the initialization.
   * @throws DatabaseException
   */
  public static void initializeDatabase(String defaultCatalogName, String defaultSchemaName, Database database)
              throws DatabaseException {
      if (((defaultCatalogName != null) || (defaultSchemaName != null)) && !(database.getConnection() instanceof
              OfflineConnection)) {
          if (database instanceof OracleDatabase) {
              String schema = defaultCatalogName;
              if (schema == null) {
                  schema = defaultSchemaName;
              }
              Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database).execute(
                  new RawSqlStatement("ALTER SESSION SET CURRENT_SCHEMA=" +
                      database.escapeObjectName(schema, Schema.class)));
          } else if (database instanceof PostgresDatabase && defaultSchemaName != null) {
              Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database).execute(new RawSqlStatement("SET SEARCH_PATH TO " + database.escapeObjectName(defaultSchemaName, Schema.class)));
          } else if (database instanceof AbstractDb2Database) {
              String schema = defaultCatalogName;
              if (schema == null) {
                  schema = defaultSchemaName;
              }
              Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database).execute(new RawSqlStatement("SET CURRENT SCHEMA "
                      + schema));
          } else if (database instanceof MySQLDatabase) {
              String schema = defaultCatalogName;
              if (schema == null) {
                  schema = defaultSchemaName;
              }
              Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database).execute(new RawSqlStatement("USE " + schema));
          }

      }
  }

}
