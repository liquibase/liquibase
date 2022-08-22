package liquibase.ext.bigquery.snapshot.jvm;

import liquibase.CatalogAndSchema;
import liquibase.Scope;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.exception.DatabaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.ext.bigquery.database.BigqueryDatabase;
import liquibase.snapshot.*;
import liquibase.snapshot.jvm.TableSnapshotGenerator;
import liquibase.statement.core.CreateTableStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.util.BooleanUtil;
import liquibase.util.StringUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class BigQueryTableSnapshotGenerator extends TableSnapshotGenerator {

    public BigQueryTableSnapshotGenerator(){}

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        int priority = super.getPriority(objectType, database);
        if (database instanceof BigqueryDatabase) {
            priority += PRIORITY_DATABASE;
        }
        return priority;
    }


    /*
    @Override
    public Class<? extends SnapshotGenerator>[] replaces() {
        return new Class[] { BigQueryTableSnapshotGenerator.class };
    }
    */


    /*
    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException {
        if (!snapshot.getSnapshotControl().shouldInclude(Table.class)) {
            return;
        }
        if (foundObject instanceof Schema) {

            Database database = snapshot.getDatabase();
            Schema schema = (Schema) foundObject;

            String query = String.format("SELECT TABLE_NAME, TABLE_SCHEMA, NULL AS COMMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '%s';",
                    database.getDefaultSchemaName());
            Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc",
                    database);
            List<Map<String, ?>> returnList = executor.queryForList(new RawSqlStatement(query));

            for (Map<String, ?> tablePropertiesMap : returnList) {
                try {
                    schema.addDatabaseObject(readTable(tablePropertiesMap, database));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    */

/*
    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException {

        Database database = snapshot.getDatabase();
        System.out.println("Database: "+database);

        String query =
                String.format("SELECT TABLE_NAME, KEYSPACE_NAME, COMMENT FROM system_schema.tables WHERE keyspace_name = '%s' AND TABLE_NAME = '%s'",
                        database.getDefaultCatalogName(), example.getName().toLowerCase());


        List<Map<String, ?>> returnList = Scope.getCurrentScope().getSingleton(ExecutorService.class)
                .getExecutor("jdbc", database).queryForList(new RawSqlStatement(query));
        if (returnList.size() != 1) {
            Scope.getCurrentScope().getLog(BigQueryTableSnapshotGenerator.class).warning(String.format(
                    "expecting exactly 1 table with name %s, got %s", example.getName(), returnList.size()));
            return null;
        } else {
            try {
                return readTable((CachedRow) returnList.get(0), database);
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
        return null;

    }
*/

/* TOOOOOO DZIALA ALE NA POTRZEBY TESTU
    @Override
    protected Table readTable(CachedRow tableMetadataResultSet, Database database) throws SQLException, DatabaseException {

        String rawTableName = tableMetadataResultSet.getString("TABLE_NAME");
        System.out.println("Table snapshot for table "+rawTableName);
        String rawSchemaName = StringUtil.trimToNull(tableMetadataResultSet.getString("TABLE_SCHEM"));
        String rawCatalogName = StringUtil.trimToNull(tableMetadataResultSet.getString("TABLE_CAT"));
        String remarks = StringUtil.trimToNull(tableMetadataResultSet.getString("REMARKS"));
        String tablespace = StringUtil.trimToNull(tableMetadataResultSet.getString("TABLESPACE_NAME"));
        String defaultTablespaceString = StringUtil.trimToNull(tableMetadataResultSet.getString("DEFAULT_TABLESPACE"));
        if (remarks != null) {
            remarks = remarks.replace("''", "'");
        }

        Table table = (new Table()).setName(this.cleanNameFromDatabase(rawTableName, database));
        table.setRemarks(remarks);
        table.setTablespace(tablespace);
        table.setDefaultTablespace(BooleanUtil.isTrue(Boolean.parseBoolean(defaultTablespaceString)));

        CatalogAndSchema schemaFromJdbcInfo = ((AbstractJdbcDatabase)database).getSchemaFromJdbcInfo(rawCatalogName, rawSchemaName);
        table.setSchema(new Schema(schemaFromJdbcInfo.getCatalogName(), schemaFromJdbcInfo.getSchemaName()));

        if ("Y".equals(tableMetadataResultSet.getString("TEMPORARY"))) {
            table.setAttribute("temporary", "GLOBAL");
            String duration = tableMetadataResultSet.getString("DURATION");
            if (duration != null && "SYS$TRANSACTION".equals(duration)) {
                table.setAttribute("duration", "ON COMMIT DELETE ROWS");
            } else if (duration != null && "SYS$SESSION".equals(duration)) {
                table.setAttribute("duration", "ON COMMIT PRESERVE ROWS");
            }
        }

  //      String columns  = ((BigqueryDatabase)database).executeSQL("select partition_expression from tables where table_name ='"+table.getName()+"'");
  //      if((partitionby != null)&&(!partitionby.isEmpty()))
  //          table.setAttribute("columns",partitionby);

        return table;
    }
*/


}

