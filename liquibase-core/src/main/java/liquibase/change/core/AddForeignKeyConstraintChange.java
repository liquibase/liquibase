package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddForeignKeyConstraintStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.ForeignKeyConstraintType;
import liquibase.structure.core.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds a foreign key constraint to an existing column.
 */
@DatabaseChange(name="addForeignKeyConstraint",
                description = "Adds a foreign key constraint to an existing column",
                priority = ChangeMetaData.PRIORITY_DEFAULT,
                appliesTo = "column")
public class AddForeignKeyConstraintChange extends AbstractChange {

    private String baseTableCatalogName;
    private String baseTableSchemaName;
    private String baseTableName;
    private String baseColumnNames;

    private String referencedTableCatalogName;
    private String referencedTableSchemaName;
    private String referencedTableName;
    private String referencedColumnNames;

    private String constraintName;

    private Boolean deferrable;
    private Boolean initiallyDeferred;
    private Boolean shouldValidate;

    private String onUpdate;
    private String onDelete;

    @Override
    protected String[] createSupportedDatabasesMetaData(
        String parameterName, DatabaseChangeProperty changePropertyAnnotation) {
        if ("deferrable".equals(parameterName) || "initiallyDeferred".equals(parameterName)) {
            List<String> supported = new ArrayList<>();
            for (Database database : DatabaseFactory.getInstance().getImplementedDatabases()) {
                if (database.supportsInitiallyDeferrableColumns()) {
                    supported.add(database.getShortName());
                }
            }
            return supported.toArray(new String[supported.size()]);

        } else {
            return super.createSupportedDatabasesMetaData(parameterName, changePropertyAnnotation);
        }
    }

    @DatabaseChangeProperty(since = "3.0", mustEqualExisting ="column.relation.catalog")
    public String getBaseTableCatalogName() {
        return baseTableCatalogName;
    }

    public void setBaseTableCatalogName(String baseTableCatalogName) {
        this.baseTableCatalogName = baseTableCatalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="column.relation.schema")
    public String getBaseTableSchemaName() {
        return baseTableSchemaName;
    }

    public void setBaseTableSchemaName(String baseTableSchemaName) {
        this.baseTableSchemaName = baseTableSchemaName;
    }

    @DatabaseChangeProperty(
        description = "Name of the table containing the column to constrain",
        exampleValue = "address",
        mustEqualExisting = "column.relation"
    )
    public String getBaseTableName() {
        return baseTableName;
    }

    public void setBaseTableName(String baseTableName) {
        this.baseTableName = baseTableName;
    }

    @DatabaseChangeProperty(
        description = "Name of column(s) to place the foreign key constraint on. Comma-separate if multiple",
        exampleValue = "person_id",
        mustEqualExisting = "column"
    )
    public String getBaseColumnNames() {
        return baseColumnNames;
    }

    public void setBaseColumnNames(String baseColumnNames) {
        this.baseColumnNames = baseColumnNames;
    }

    @DatabaseChangeProperty(since = "3.0", mustEqualExisting = "column")
    public String getReferencedTableCatalogName() {
        return referencedTableCatalogName;
    }

    public void setReferencedTableCatalogName(String referencedTableCatalogName) {
        this.referencedTableCatalogName = referencedTableCatalogName;
    }

    public String getReferencedTableSchemaName() {
        return referencedTableSchemaName;
    }

    public void setReferencedTableSchemaName(String referencedTableSchemaName) {
        this.referencedTableSchemaName = referencedTableSchemaName;
    }

    @DatabaseChangeProperty(
        description = "Name of the table the foreign key points to",
        exampleValue = "person")
    public String getReferencedTableName() {
        return referencedTableName;
    }

    public void setReferencedTableName(String referencedTableName) {
        this.referencedTableName = referencedTableName;
    }

    @DatabaseChangeProperty(
        description = "Column(s) the foreign key points to. Comma-separate if multiple",
        exampleValue = "id")
    public String getReferencedColumnNames() {
        return referencedColumnNames;
    }

    public void setReferencedColumnNames(String referencedColumnNames) {
        this.referencedColumnNames = referencedColumnNames;
    }

    @DatabaseChangeProperty(description = "Name of the new foreign key constraint", exampleValue = "fk_address_person")
    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }

    @DatabaseChangeProperty(description = "Is the foreign key deferrable")
    public Boolean getDeferrable() {
        return deferrable;
    }

    public void setDeferrable(Boolean deferrable) {
        this.deferrable = deferrable;
    }

    @DatabaseChangeProperty(description = "Is the foreign key initially deferred")
    public Boolean getInitiallyDeferred() {
        return initiallyDeferred;
    }

    /**
     * In Oracle PL/SQL, the VALIDATE keyword defines whether a foreign key constraint on a column in a table
     * should be checked if it refers to a valid row or not.
     * @return true if ENABLE VALIDATE (this is the default), or false if ENABLE NOVALIDATE.
     */
    @DatabaseChangeProperty(description = "This is true if the foreign key has 'ENABLE VALIDATE' set, or false if the foreign key has 'ENABLE NOVALIDATE' set.")
    public Boolean getValidate() {
        return shouldValidate;
    }

    /**
     *
     * @param shouldValidate - if shouldValidate is set to FALSE then the constraint will be created
     * with the 'ENABLE NOVALIDATE' mode. This means the constraint would be created, but that no
     * check will be done to ensure old data has valid foreign keys - only new data would be checked
     * to see if it complies with the constraint logic. The default state for foreign keys is to
     * have 'ENABLE VALIDATE' set.
     */
    public void setValidate(Boolean shouldValidate) {
        this.shouldValidate = shouldValidate;
    }
    
    public void setInitiallyDeferred(Boolean initiallyDeferred) {
        this.initiallyDeferred = initiallyDeferred;
    }

    public void setDeleteCascade(Boolean deleteCascade) {
        if ((deleteCascade != null) && deleteCascade) {
            setOnDelete("CASCADE");
        }
    }

    public void setOnUpdate(String rule) {
        this.onUpdate = rule;
    }

    @DatabaseChangeProperty(
        description = "ON UPDATE functionality. Possible values: 'CASCADE', 'SET NULL', 'SET DEFAULT', " +
            "'RESTRICT', 'NO ACTION'",
        exampleValue = "RESTRICT")
    public String getOnUpdate() {
        return onUpdate;
    }

    public void setOnDelete(String onDelete) {
        this.onDelete = onDelete;
    }

    @DatabaseChangeProperty(description = "ON DELETE functionality. Possible values: 'CASCADE', 'SET NULL', " +
        "'SET DEFAULT', 'RESTRICT', 'NO ACTION'",
        exampleValue = "CASCADE")
    public String getOnDelete() {
        return this.onDelete;
    }

    public void setOnDelete(ForeignKeyConstraintType rule) {
        if (rule == null) {
            //nothing
        } else if (rule == ForeignKeyConstraintType.importedKeyCascade) {
            setOnDelete("CASCADE");
        } else if (rule == ForeignKeyConstraintType.importedKeySetNull) {
            setOnDelete("SET NULL");
        } else if (rule == ForeignKeyConstraintType.importedKeySetDefault) {
            setOnDelete("SET DEFAULT");
        } else if (rule == ForeignKeyConstraintType.importedKeyRestrict) {
            setOnDelete("RESTRICT");
        } else if (rule == ForeignKeyConstraintType.importedKeyNoAction){
            setOnDelete("NO ACTION");
        } else {
            throw new UnexpectedLiquibaseException("Unknown onDelete action: "+rule);
        }
    }

    public void setOnUpdate(ForeignKeyConstraintType rule) {
        if (rule == null) {
            //nothing
        } else if (rule == ForeignKeyConstraintType.importedKeyCascade) {
            setOnUpdate("CASCADE");
        } else  if (rule == ForeignKeyConstraintType.importedKeySetNull) {
            setOnUpdate("SET NULL");
        } else if (rule == ForeignKeyConstraintType.importedKeySetDefault) {
            setOnUpdate("SET DEFAULT");
        } else if (rule == ForeignKeyConstraintType.importedKeyRestrict) {
            setOnUpdate("RESTRICT");
        } else if (rule == ForeignKeyConstraintType.importedKeyNoAction) {
            setOnUpdate("NO ACTION");
        } else {
            throw new UnexpectedLiquibaseException("Unknown onUpdate action: "+onUpdate);
        }
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {

        boolean deferrable = false;
        if (getDeferrable() != null) {
            deferrable = getDeferrable();
        }

        boolean initiallyDeferred = false;
        if (getInitiallyDeferred() != null) {
            initiallyDeferred = getInitiallyDeferred();
        }

        boolean shouldValidate = true;
        if (getValidate() != null) {
            shouldValidate = getValidate();
        }

        return new SqlStatement[]{
                new AddForeignKeyConstraintStatement(getConstraintName(),
                        getBaseTableCatalogName(),
                        getBaseTableSchemaName(),
                        getBaseTableName(),
                        ColumnConfig.arrayFromNames(getBaseColumnNames()),
                        getReferencedTableCatalogName(),
                        getReferencedTableSchemaName(),
                        getReferencedTableName(),
                        ColumnConfig.arrayFromNames(getReferencedColumnNames()))
                        .setDeferrable(deferrable)
                        .setInitiallyDeferred(initiallyDeferred)
                        .setOnUpdate(getOnUpdate())
                        .setOnDelete(getOnDelete())
                        .setShouldValidate(shouldValidate)
        };
    }

    @Override
    protected Change[] createInverses() {
        DropForeignKeyConstraintChange inverse = new DropForeignKeyConstraintChange();
        inverse.setBaseTableSchemaName(getBaseTableSchemaName());
        inverse.setBaseTableName(getBaseTableName());
        inverse.setConstraintName(getConstraintName());

        return new Change[]{
                inverse
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        ChangeStatus result = new ChangeStatus();
        try {
            ForeignKey example = new ForeignKey(
                getConstraintName(),
                getBaseTableCatalogName(),
                getBaseTableSchemaName(),
                getBaseTableName()
            );
            example.setPrimaryKeyTable(
                new Table(getReferencedTableCatalogName(), getReferencedTableSchemaName(), getReferencedTableName())
            );
            example.setForeignKeyColumns(Column.listFromNames(getBaseColumnNames()));
            example.setPrimaryKeyColumns(Column.listFromNames(getReferencedColumnNames()));

            ForeignKey snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(example, database);
            result.assertComplete(snapshot != null, "Foreign key does not exist");

            if (snapshot != null) {
                if (getInitiallyDeferred() != null) {
                    result.assertCorrect(
                        getInitiallyDeferred().equals(snapshot.isInitiallyDeferred()),
                        "Initially deferred incorrect"
                    );
                }
                if (getDeferrable() != null) {
                    result.assertCorrect(
                        getDeferrable().equals(snapshot.isDeferrable()),
                        "Initially deferred incorrect"
                    );
                }
                if (getValidate() != null) {
                    result.assertCorrect(getValidate().equals(snapshot.shouldValidate()), "validate incorrect");
                }
            }

            return result;
        } catch (Exception e) {
            return result.unknown(e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "Foreign key constraint added to " + getBaseTableName() + " (" + getBaseColumnNames() + ")";
    }

    /**
     * @deprecated No longer supported in 3.0
     */
    @Deprecated
    public Boolean getReferencesUniqueColumn() {
        return null;
    }

    public void setReferencesUniqueColumn(Boolean referencesUniqueColumn) {
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
