package liquibase.snapshot.jvm;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.core.Column;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoIncrementSequencesCache {

    private Map<String, Column.AutoIncrementInformation> autoIncrementColumns;


    public Map<String, Column.AutoIncrementInformation> obtainSequencesInformation(Database database, DatabaseSnapshot snapshot) {
        if (autoIncrementColumns != null) {
            return autoIncrementColumns;
        } else {
            autoIncrementColumns = new HashMap<>();
            Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
            try {
                String query = this.getQueryForDatabase(database);
                List<Map<String, ?>> rows = executor.queryForList(new RawSqlStatement(query));
                for (Map<String, ?> row : rows) {
                    String schemaName = (String) row.get("SCHEMA_NAME");
                    String tableName = (String) row.get("TABLE_NAME");
                    String columnName = (String) row.get("COLUMN_NAME");
                    Long startValue = (Long) row.get("START_VALUE");
                    Long incrementBy = (Long) row.get("INCREMENT_BY");

                    Column.AutoIncrementInformation info = new Column.AutoIncrementInformation(startValue, incrementBy);
                    autoIncrementColumns.put(String.format("%s.%s.%s", schemaName, tableName, columnName), info);
                }
                snapshot.setScratchData("autoIncrementColumns", autoIncrementColumns);
            } catch (DatabaseException e) {
                Scope.getCurrentScope().getLog(getClass()).info("Could not read identity information", e);
            } catch (LiquibaseException e) {
                Scope.getCurrentScope().getLog(getClass()).fine(e.getMessage());
                return autoIncrementColumns;
            }
        }
        return autoIncrementColumns;
    }

    private String getQueryForDatabase(Database database) throws LiquibaseException {
        if (database instanceof MSSQLDatabase) {
            return "SELECT object_schema_name(object_id) AS schema_name, " +
                    "object_name(object_id) AS table_name, name AS column_name, " +
                    "CAST(seed_value AS bigint) AS start_value, " +
                    "CAST(increment_value AS bigint) AS increment_by " +
                    "FROM sys.identity_columns";
        } else if (database instanceof PostgresDatabase) {
            int version = 9;
            try {
                version = database.getDatabaseMajorVersion();
            } catch (Exception e) {
                Scope.getCurrentScope().getLog(getClass()).warning("Failed to retrieve database version: " + e);
            }
            if (version < 10) { // 'pg_sequence' view does not exists yet
                return "SELECT " +
                        "    ns.nspname as SCHEMA_NAME, " +
                        "    td.relname as TABLE_NAME, " +
                        "    pa.attname as COLUMN_NAME, " +
                        "    s.start_value::bigint AS START_VALUE, " +
                        "    s.increment::bigint AS INCREMENT_BY " +
                        "FROM pg_class c " +
                        "    JOIN pg_namespace ns on c.relnamespace = ns.oid " +
                        "    JOIN information_schema.sequences s on c.relname = s.sequence_name AND ns.nspname = s.sequence_schema::varchar" +
                        "    JOIN pg_depend d ON c.oid = d.objid " +
                        "    JOIN pg_class td ON td.oid = d.refobjid " +
                        "    JOIN pg_attribute pa ON  pa.attrelid=td.oid AND pa.attnum=d.refobjsubid " +
                        "WHERE c.relkind = 'S' AND d.deptype = 'a'";
            } else {
                return "SELECT " +
                        "    ns.nspname as SCHEMA_NAME, " +
                        "    td.relname as TABLE_NAME, " +
                        "    pa.attname as COLUMN_NAME, " +
                        "    COALESCE(pg_sequence_last_value(c.oid::regclass) + s.seqincrement, " +
                        "      s.seqstart) AS START_VALUE, " +
                        "    s.seqincrement AS INCREMENT_BY " +
                        "FROM pg_class c " +
                        "    JOIN pg_sequence s on c.oid = s.seqrelid " +
                        "    JOIN pg_namespace ns on c.relnamespace = ns.oid " +
                        "    JOIN pg_depend d ON c.oid = d.objid " +
                        "    JOIN pg_class td ON td.oid = d.refobjid " +
                        "    JOIN pg_attribute pa ON  pa.attrelid=td.oid AND pa.attnum=d.refobjsubid " +
                        "WHERE c.relkind = 'S' AND d.deptype = 'a'";
            }
        }

        throw new LiquibaseException("Liquibase or the database do not support auto increment parameters.");
    }
}
