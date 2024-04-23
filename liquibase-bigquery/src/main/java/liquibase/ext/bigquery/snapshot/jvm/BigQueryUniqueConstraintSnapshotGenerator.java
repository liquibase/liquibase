package liquibase.ext.bigquery.snapshot.jvm;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.ext.bigquery.database.BigqueryDatabase;
import liquibase.snapshot.CachedRow;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotGenerator;
import liquibase.snapshot.jvm.UniqueConstraintSnapshotGenerator;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BigQueryUniqueConstraintSnapshotGenerator extends UniqueConstraintSnapshotGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (!(database instanceof BigqueryDatabase)) {
            return PRIORITY_NONE;
        }
        int priority = super.getPriority(objectType, database);
        if (priority > PRIORITY_NONE && database instanceof BigqueryDatabase) {
            priority += PRIORITY_DATABASE;
        }
        return priority;
    }

    @Override
    public Class<? extends SnapshotGenerator>[] replaces() {
        return new Class[] { UniqueConstraintSnapshotGenerator.class };
    }

    @Override
    protected List<CachedRow> listConstraints(Table table, DatabaseSnapshot snapshot, Schema schema) throws DatabaseException, SQLException {
        Scope.getCurrentScope().getLog(this.getClass()).info("Constraints not supported by BigQuery");
        return new ArrayList<>(); //new BigQueryResultSetConstraintsExtractor(snapshot, schema.getCatalogName(), schema.getName(), table.getName())).fastFetch();
    }

    @Override
    protected List<Map<String, ?>> listColumns(UniqueConstraint example, Database database, DatabaseSnapshot snapshot) throws DatabaseException {
        Relation table = example.getRelation();
        Schema schema = table.getSchema();
        String name = example.getName();
        String schemaName = database.correctObjectName(schema.getName(), Schema.class);
        String constraintName = database.correctObjectName(name, UniqueConstraint.class);
        String tableName = database.correctObjectName(table.getName(), Table.class);

        String sql = "select CONSTRAINT_NAME, CONSTRAINT_NAME as COLUMN_NAME FROM " + database.getSystemSchema() + ".TABLE_CONSTRAINTS WHERE CONSTRAINT_TYPE='UNIQUE'";
        if (schemaName != null) {
            sql = sql + "and CONSTRAINT_SCHEMA='" + schemaName + "' ";
        }

        if (tableName != null) {
            sql = sql + "AND TABLE_NAME='" + tableName + "' ";
        }

        if (constraintName != null) {
            sql = sql + "AND CONSTRAINT_NAME='" + constraintName + "'";
        }

        return Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database).queryForList(new RawSqlStatement(sql));
    }
}

