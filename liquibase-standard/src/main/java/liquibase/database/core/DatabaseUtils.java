package liquibase.database.core;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.OfflineConnection;
import liquibase.exception.DatabaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.core.Schema;
import liquibase.util.StringUtil;

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
            final Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);

            if (database instanceof OracleDatabase) {
                String schema = defaultCatalogName;
                if (schema == null) {
                    schema = defaultSchemaName;
                }
                executor.execute(
                        new RawSqlStatement("ALTER SESSION SET CURRENT_SCHEMA=" +
                                database.escapeObjectName(schema, Schema.class)));
            } else if (database instanceof PostgresDatabase && defaultSchemaName != null) {
                String searchPath = executor.queryForObject(new RawSqlStatement("SHOW SEARCH_PATH"), String.class);

                if (!searchPath.equals(defaultCatalogName) && !searchPath.equals(defaultSchemaName) && !searchPath.equals("\"" + defaultSchemaName + "\"") && !searchPath.startsWith(defaultSchemaName + ",") && !searchPath.startsWith("\"" + defaultSchemaName + "\",")) {
                    String finalSearchPath;
                    if (GlobalConfiguration.PRESERVE_SCHEMA_CASE.getCurrentValue()) {
                        finalSearchPath = ((PostgresDatabase) database).quoteObject(defaultSchemaName, Schema.class);
                    } else {
                        finalSearchPath = defaultSchemaName;
                    }

                    if (StringUtil.isNotEmpty(searchPath)) {
                        //If existing search path entries are not quoted, quote them. Some databases do not show them as quoted even though they need to be (like $user or case sensitive schemas)
                        finalSearchPath += ", " + StringUtil.join(StringUtil.splitAndTrim(searchPath, ","), ",", (StringUtil.StringUtilFormatter<String>) obj -> {
                            if (obj.startsWith("\"")) {
                                return obj;
                            }
                            return ((PostgresDatabase) database).quoteObject(obj, Schema.class);
                        });
                    }

                    executor.execute(new RawSqlStatement("SET SEARCH_PATH TO " + finalSearchPath));
                }

            } else if (database instanceof AbstractDb2Database) {
                String schema = defaultCatalogName;
                if (schema == null) {
                    schema = defaultSchemaName;
                }
                executor.execute(new RawSqlStatement("SET CURRENT SCHEMA "
                        + schema));
            } else if (database instanceof MySQLDatabase) {
                String schema = defaultCatalogName;
                if (schema == null) {
                    schema = defaultSchemaName;
                }
                executor.execute(new RawSqlStatement("USE " + schema));
            } else if (database instanceof MSSQLDatabase) {
                    defaultCatalogName = StringUtil.trimToNull(defaultCatalogName);
                    if (defaultCatalogName != null) {
                        executor.execute(new RawSqlStatement(String.format("USE %s", defaultCatalogName)));
                    }
            }
        }
    }

    /**
     * Build a string containing the catalog and schema, separated by a period, if they are not empty.
     * @return the built string, or an empty string if both catalog and schema are empty
     */
    public static String buildCatalogAndSchemaString(String catalog, String schema) {
        String info = "";
        if (StringUtil.isNotEmpty(catalog)) {
            info += catalog;
        }
        if (StringUtil.isNotEmpty(schema)) {
            if (!info.endsWith(".")) {
                info += ".";
            }
            info += schema;
        }
        return info;
    }

}
