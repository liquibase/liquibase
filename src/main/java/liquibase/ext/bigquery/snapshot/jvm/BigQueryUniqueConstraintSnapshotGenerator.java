package liquibase.ext.bigquery.snapshot.jvm;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.core.H2Database;
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
import liquibase.util.StringUtil;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BigQueryUniqueConstraintSnapshotGenerator extends UniqueConstraintSnapshotGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        int priority = super.getPriority(objectType, database);
        if (database instanceof BigqueryDatabase) {
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
        return (new BigQueryResultSetConstraintsExtractor(snapshot, schema.getCatalogName(), schema.getName(), table.getName())).fastFetch();
    }

    @Override
    protected List<Map<String, ?>> listColumns(UniqueConstraint example, Database database, DatabaseSnapshot snapshot) throws DatabaseException {
        Relation table = example.getRelation();
        Schema schema = table.getSchema();
        String name = example.getName();
        String schemaName = database.correctObjectName(schema.getName(), Schema.class);
        String constraintName = database.correctObjectName(name, UniqueConstraint.class);
        String tableName = database.correctObjectName(table.getName(), Table.class);

        String sql = "select CONSTRAINT_NAME, CONSTRAINT_NAME as COLUMN_NAME from " + database.getSystemSchema() + ".TABLE_CONSTRAINTS where CONSTRAINT_TYPE='UNIQUE'";
        if (schemaName != null) {
            sql = sql + "and CONSTRAINT_SCHEMA='" + schemaName + "' ";
        }

        if (tableName != null) {
            sql = sql + "and TABLE_NAME='" + tableName + "' ";
        }

        if (constraintName != null) {
            sql = sql + "and CONSTRAINT_NAME='" + constraintName + "'";
        }

        return ((ExecutorService) Scope.getCurrentScope().getSingleton(ExecutorService.class)).getExecutor("jdbc", database).queryForList(new RawSqlStatement(sql));
    }

    private void setValidateOptionIfAvailable(Database database, UniqueConstraint uniqueConstraint, Map<String, ?> columnsMetadata) {
        if (database instanceof BigqueryDatabase) {
            Object constraintValidate = columnsMetadata.get("CONSTRAINT_VALIDATE");
            String VALIDATE = "VALIDATED";
            if (constraintValidate != null && !constraintValidate.toString().trim().isEmpty()) {
                uniqueConstraint.setShouldValidate("VALIDATED".equals(this.cleanNameFromDatabase(constraintValidate.toString().trim(), database)));
            }

        }
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException {
        Database database = snapshot.getDatabase();
        UniqueConstraint exampleConstraint = (UniqueConstraint)example;
        Relation table = exampleConstraint.getRelation();

        List<Map<String, ?>> metadata = this.listColumns(exampleConstraint, database, snapshot);
        if (metadata.isEmpty()) {
            return null;
        } else {
            UniqueConstraint constraint = new UniqueConstraint();
            constraint.setRelation(table);
            constraint.setName(example.getName());
            constraint.setBackingIndex(exampleConstraint.getBackingIndex());
            constraint.setInitiallyDeferred(((UniqueConstraint)example).isInitiallyDeferred());
            constraint.setDeferrable(((UniqueConstraint)example).isDeferrable());
            constraint.setClustered(((UniqueConstraint)example).isClustered());

            Map col;
            for(Iterator var8 = metadata.iterator(); var8.hasNext(); setValidateOptionIfAvailable(database, constraint, col)) {
                col = (Map)var8.next();
                String ascOrDesc = (String)col.get("ASC_OR_DESC");
                Boolean descending = "D".equals(ascOrDesc) ? Boolean.TRUE : ("A".equals(ascOrDesc) ? Boolean.FALSE : null);
                if (database instanceof H2Database) {
                    Iterator var12 = StringUtil.splitAndTrim((String)col.get("COLUMN_NAME"), ",").iterator();

                    while(var12.hasNext()) {
                        String columnName = (String)var12.next();
                        constraint.getColumns().add((new Column(columnName)).setDescending(descending).setRelation(table));
                    }
                } else {
                    constraint.getColumns().add((new Column((String)col.get("COLUMN_NAME"))).setDescending(descending).setRelation(table));
                }
            }

            return constraint;
        }
    }

}

