package liquibase.change.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import liquibase.CatalogAndSchema;
import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.structure.core.Index;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Table;

@DatabaseChange(name = "dropAllIndexes", description = "Drops all indexes for a table", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table")
public class DropAllIndexesChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String tableName;

    @DatabaseChangeProperty(mustEqualExisting = "table.catalog", description = "Name of the table containing indexes", since = "3.6")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "table.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "table", requiredForDatabase = "all")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        List<SqlStatement> sqlStatements = new ArrayList<>();

        List<DropIndexChange> childDropChanges = generateChildren(database);

        for (DropIndexChange change : childDropChanges) {
            sqlStatements.addAll(Arrays.asList(change.generateStatements(database)));
        }

        return sqlStatements.toArray(new SqlStatement[sqlStatements.size()]);
    }

    @Override
    public String getConfirmationMessage() {
        return "Indexes on table " + getTableName() + " dropped";
    }

    /**
     * Iterates through all the INDEXes of the target table and outputs a list of DropIndexChanges
     * for a given database type.
     *
     * @param database the database type for which subchanges need to be generated
     * @return the list of generated DropIndexChanges
     */
    private List<DropIndexChange> generateChildren(Database database) {
        // Make a new list
        List<DropIndexChange> childDropChanges = new ArrayList<>();

        try {
            SnapshotControl control = new SnapshotControl(database);
            control.getTypesToInclude().add(Index.class);
            CatalogAndSchema catalogAndSchema = new CatalogAndSchema(getCatalogName(), getSchemaName());
            catalogAndSchema = catalogAndSchema.standardize(database);
            Table target = SnapshotGeneratorFactory.getInstance().createSnapshot(new Table(catalogAndSchema.getCatalogName(), catalogAndSchema.getSchemaName(), database.correctObjectName(getTableName(), Table.class)), database);

            List<Index> results = ((target == null) ? null : target.getIndexes());
            Set<String> handledConstraints = new HashSet<>();

            if ((results != null) && (!results.isEmpty())) {
                for (Index ind : results) {
                    Relation baseTable = ind.getTable();
                    String constraintName = ind.getName();
                    if (DatabaseObjectComparatorFactory.getInstance().isSameObject(baseTable, target, null, database)) {
                        if (!handledConstraints.contains(constraintName)) {
                            DropIndexChange dropIndexChange = new DropIndexChange();

                            dropIndexChange.setSchemaName(getSchemaName());
                            dropIndexChange.setTableName(tableName);
                            dropIndexChange.setIndexName(constraintName);

                            childDropChanges.add(dropIndexChange);
                            handledConstraints.add(constraintName);
                        }
                    }
                    else {
                        throw new UnexpectedLiquibaseException("Expected to return only indexes for table name: " + getTableName() + " and got results for table: " + baseTable.getName());
                    }
                }
            }

            return childDropChanges;
        }
        catch (DatabaseException | InvalidExampleException e) {
            throw new UnexpectedLiquibaseException("Failed to find indexes for table: " + getTableName(), e);
        }
    }

    @Override
    public boolean generateStatementsVolatile(Database database) {
        return true;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
