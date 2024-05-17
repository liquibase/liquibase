package liquibase.snapshot.jvm;

import liquibase.CatalogAndSchema;
import liquibase.Scope;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.BigQueryDatabase;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotGenerator;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;

import java.util.*;

public class BigQueryForeignKeySnapshotGenerator extends ForeignKeySnapshotGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (database instanceof BigQueryDatabase) {
            return super.getPriority(objectType, database) + PRIORITY_DATABASE;
        } else {
            return PRIORITY_NONE;
        }
    }

    @Override
    public Class<? extends SnapshotGenerator>[] replaces() {
        return new Class[]{ForeignKeySnapshotGenerator.class};
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException {
        Database database = snapshot.getDatabase();
        Table fkTable = ((ForeignKey) example).getForeignKeyTable();
        String searchTableName = database.correctObjectName(fkTable.getName(), Table.class);
        String fkFullName = searchTableName + "." + example.getName();
        String searchCatalog = ((AbstractJdbcDatabase) database).getJdbcCatalogName(fkTable.getSchema());
        String searchSchema = ((AbstractJdbcDatabase) database).getJdbcSchemaName(fkTable.getSchema());
        String systemSchema = database.getSystemSchema().toUpperCase();

        String query = new StringBuilder("SELECT ")
                .append("TC.CONSTRAINT_NAME as CONSTRAINT_NAME, ")
                .append("KCU.TABLE_CATALOG as FOREIGN_KEY_TABLE_CATALOG, ")
                .append("KCU.TABLE_SCHEMA as FOREIGN_KEY_TABLE_SCHEMA, ")
                .append("KCU.TABLE_NAME as FOREIGN_KEY_TABLE, ")
                .append("KCU.COLUMN_NAME as FOREIGN_KEY_COLUMN, ")
                .append("CCU.TABLE_CATALOG as PRIMARY_KEY_TABLE_CATALOG, ")
                .append("CCU.TABLE_SCHEMA as PRIMARY_KEY_TABLE_SCHEMA, ")
                .append("CCU.TABLE_NAME as PRIMARY_KEY_TABLE, ")
                .append("CCU.COLUMN_NAME as PRIMARY_KEY_COLUMN ")
                .append(String.format("FROM %1$s.%2$s.TABLE_CONSTRAINTS as TC JOIN %1$s.%2$s.CONSTRAINT_COLUMN_USAGE as CCU on " +
                                "TC.CONSTRAINT_NAME=CCU.CONSTRAINT_NAME JOIN %1$s.%2$s.KEY_COLUMN_USAGE as KCU on KCU.CONSTRAINT_NAME=TC.CONSTRAINT_NAME ",
                        searchSchema, systemSchema))
                .append("WHERE TC.TABLE_NAME=? AND TC.TABLE_SCHEMA=? AND TC.TABLE_CATALOG=? AND TC.CONSTRAINT_TYPE='FOREIGN KEY' AND TC.CONSTRAINT_NAME=?")
                .toString();
        List<Map<String, ?>> results = Scope.getCurrentScope().getSingleton(ExecutorService.class)
                .getExecutor("jdbc", database).queryForList(new RawParameterizedSqlStatement(query, searchTableName, searchSchema, searchCatalog, fkFullName));

        if (!results.isEmpty()) {
            ForeignKey foreignKey = null;
            for (Map<String, ?> resultMap : results) {
                Table foreignKeyTable = new Table().setName(Objects.toString(resultMap.get("FOREIGN_KEY_TABLE")));

                String foreignKeyName =
                        Optional.ofNullable((String) resultMap.get("CONSTRAINT_NAME"))
                                .map(s -> s.replace(foreignKeyTable.getName() + ".", ""))
                                .orElse("undefined");

                foreignKeyTable.setSchema(new Schema(new Catalog(Objects.toString(resultMap.get("FOREIGN_KEY_TABLE_CATALOG"))),
                        Objects.toString(resultMap.get("FOREIGN_KEY_TABLE_SCHEMA"))));

                Table primaryKeyTable = new Table().setName(Objects.toString(resultMap.get("PRIMARY_KEY_TABLE")));
                primaryKeyTable.setSchema(
                        new Schema(new Catalog(Objects.toString(resultMap.get("PRIMARY_KEY_TABLE_CATALOG"))),
                                Objects.toString(resultMap.get("PRIMARY_KEY_TABLE_SCHEMA"))));
                Column fkColumn = new Column(Objects.toString(resultMap.get("FOREIGN_KEY_COLUMN"))).setRelation(foreignKeyTable);
                Column pkColumn = new Column(Objects.toString(resultMap.get("PRIMARY_KEY_COLUMN"))).setRelation(primaryKeyTable);

                if (foreignKey != null) {
                    if (!foreignKey.getForeignKeyColumns().contains(fkColumn)) {
                        foreignKey.addForeignKeyColumn(fkColumn);
                    }
                    if (!foreignKey.getPrimaryKeyColumns().contains(pkColumn)) {
                        foreignKey.addPrimaryKeyColumn(pkColumn);
                    }
                } else {
                    foreignKey = new ForeignKey(foreignKeyName);

                    foreignKey.setForeignKeyTable(foreignKeyTable);
                    foreignKey.setPrimaryKeyTable(primaryKeyTable);

                    foreignKey.addForeignKeyColumn(fkColumn);
                    foreignKey.addPrimaryKeyColumn(pkColumn);

                }
            }
            return foreignKey;
        }
        return null;
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException {
        if (!snapshot.getSnapshotControl().shouldInclude(ForeignKey.class)) {
            return;
        }
        if (foundObject instanceof Table) {
            Table table = (Table) foundObject;
            Database database = snapshot.getDatabase();
            Schema schema = table.getSchema();

            CatalogAndSchema catalogAndSchema = (new CatalogAndSchema(schema.getCatalogName(), schema.getName())).customize(database);
            String jdbcSchemaName = database.correctObjectName(((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema), Schema.class);
            String query = String.format("SELECT CONSTRAINT_NAME FROM %s.%s.TABLE_CONSTRAINTS WHERE TABLE_NAME=? AND TABLE_SCHEMA=? AND TABLE_CATALOG=? AND " +
                    "CONSTRAINT_TYPE='FOREIGN KEY';", jdbcSchemaName, database.getSystemSchema().toUpperCase());
            List<Map<String, ?>> tableConstraints = Scope.getCurrentScope().getSingleton(ExecutorService.class)
                    .getExecutor("jdbc", database).queryForList(new RawParameterizedSqlStatement(query, table.getName(), schema.getName(),
                            schema.getCatalogName()));
            for (Map<String, ?> row : tableConstraints) {
                String foreignKeyName = Objects.toString(row.get("CONSTRAINT_NAME"));
                ForeignKey fk = new ForeignKey()
                        .setName(foreignKeyName.replace(table.getName() + ".", ""))
                        .setForeignKeyTable(table);
                table.getOutgoingForeignKeys().add(fk);
            }
        }
    }
}