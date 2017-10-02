package liquibase.change.core;

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
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Table;

import java.util.*;

@DatabaseChange(name="dropAllForeignKeyConstraints", description = "Drops all foreign key constraints for a table", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table")
public class DropAllForeignKeyConstraintsChange extends AbstractChange {

    private String baseTableCatalogName;
    private String baseTableSchemaName;
    private String baseTableName;

    @DatabaseChangeProperty(mustEqualExisting ="table.catalog", description = "Name of the table containing columns constrained by foreign keys", since = "3.0")
    public String getBaseTableCatalogName() {
        return baseTableCatalogName;
    }

    public void setBaseTableCatalogName(String baseTableCatalogName) {
        this.baseTableCatalogName = baseTableCatalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="table.schema")
    public String getBaseTableSchemaName() {
        return baseTableSchemaName;
    }

    public void setBaseTableSchemaName(String baseTableSchemaName) {
        this.baseTableSchemaName = baseTableSchemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "table", requiredForDatabase = "all")
    public String getBaseTableName() {
        return baseTableName;
    }

    public void setBaseTableName(String baseTableName) {
        this.baseTableName = baseTableName;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        List<SqlStatement> sqlStatements = new ArrayList<>();

        List<DropForeignKeyConstraintChange> childDropChanges = generateChildren(database);

        for (DropForeignKeyConstraintChange change : childDropChanges) {
            sqlStatements.addAll(Arrays.asList(change.generateStatements(database)));
        }

        return sqlStatements.toArray(new SqlStatement[sqlStatements.size()]);
    }

    @Override
    public String getConfirmationMessage() {
        return "Foreign keys on base table " + getBaseTableName() + " dropped";
    }

    /**
     * Iterates through all the FOREIGN KEYs of the target table and outputs a list of DropForeignKeyConstraintChanges
     * for a given database type.
     *
     * @param database the database type for which subchanges need to be generated
     * @return the list of generated DropForeignKeyConstraintChanges
     */
    private List<DropForeignKeyConstraintChange> generateChildren(Database database) {
        // Make a new list
        List<DropForeignKeyConstraintChange> childDropChanges = new ArrayList<>();

        try {
            SnapshotControl control = new SnapshotControl(database);
            control.getTypesToInclude().add(ForeignKey.class);
            CatalogAndSchema catalogAndSchema =
                    new CatalogAndSchema(getBaseTableCatalogName(), getBaseTableSchemaName());
            catalogAndSchema = catalogAndSchema.standardize(database);
            Table target = SnapshotGeneratorFactory.getInstance().createSnapshot(
                    new Table(catalogAndSchema.getCatalogName(), catalogAndSchema.getSchemaName(),
                            database.correctObjectName(getBaseTableName(), Table.class))
                    , database);

            List<ForeignKey> results = ((target == null) ? null : target.getOutgoingForeignKeys());
            Set<String> handledConstraints = new HashSet<>();

            if ((results != null) && (!results.isEmpty())) {
                for (ForeignKey fk : results) {
                    Table baseTable = fk.getForeignKeyTable();
                    String constraintName = fk.getName();
                    if (DatabaseObjectComparatorFactory.getInstance().isSameObject(
                            baseTable,
                            target,
                            null,
                            database
                    )) {
                        if( !handledConstraints.contains(constraintName)) {
                            DropForeignKeyConstraintChange dropForeignKeyConstraintChange =
                                    new DropForeignKeyConstraintChange();

                            dropForeignKeyConstraintChange.setBaseTableSchemaName(getBaseTableSchemaName());
                            dropForeignKeyConstraintChange.setBaseTableName(baseTableName);
                            dropForeignKeyConstraintChange.setConstraintName(constraintName);

                            childDropChanges.add(dropForeignKeyConstraintChange);
                            handledConstraints.add(constraintName);
                        }
                    } else {
                        throw new UnexpectedLiquibaseException(
                                "Expected to return only foreign keys for base table name: " +
                                        getBaseTableName() + " and got results for table: " + baseTableName);
                    }
                }
            }

            return childDropChanges;

        } catch (DatabaseException | InvalidExampleException e) {
            throw new UnexpectedLiquibaseException("Failed to find foreign keys for table: " + getBaseTableName(), e);
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
