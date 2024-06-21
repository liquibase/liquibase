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
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds a foreign key constraint to an existing column.
 */
@DatabaseChange(name = "addForeignKeyConstraint",
                description = "Adds a foreign key constraint to an existing column",
                priority = ChangeMetaData.PRIORITY_DEFAULT,
                appliesTo = "column")
public class AddForeignKeyConstraintChange extends AbstractChange {

    @Setter
    private String baseTableCatalogName;
    @Setter
    private String baseTableSchemaName;
    @Setter
    private String baseTableName;
    @Setter
    private String baseColumnNames;

    @Setter
    private String referencedTableCatalogName;
    @Setter
    private String referencedTableSchemaName;
    @Setter
    private String referencedTableName;
    @Setter
    private String referencedColumnNames;

    @Setter
    private String constraintName;

    @Setter
    private Boolean deferrable;
    @Setter
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
            return supported.toArray(new String[0]);

        } else {
            return super.createSupportedDatabasesMetaData(parameterName, changePropertyAnnotation);
        }
    }

    @DatabaseChangeProperty(since = "3.0", mustEqualExisting ="column.relation.catalog",
        description = "Name of the database catalog of the base table")
    public String getBaseTableCatalogName() {
        return baseTableCatalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="column.relation.schema", description = "Name of the database schema of the base table")
    public String getBaseTableSchemaName() {
        return baseTableSchemaName;
    }

    @DatabaseChangeProperty(
        description = "Name of the table containing the column to constrain",
        exampleValue = "address",
        mustEqualExisting = "column.relation"
    )
    public String getBaseTableName() {
        return baseTableName;
    }

    @DatabaseChangeProperty(
        description = "Name of the column(s) to place the foreign key constraint on. Comma-separate if multiple",
        exampleValue = "person_id",
        mustEqualExisting = "column"
    )
    public String getBaseColumnNames() {
        return baseColumnNames;
    }

    @DatabaseChangeProperty(since = "3.0", mustEqualExisting = "column",
        description = "Name of the database catalog of the referenced table")
    public String getReferencedTableCatalogName() {
        return referencedTableCatalogName;
    }

    @DatabaseChangeProperty(description = "Name of the database schema of the referenced table")
    public String getReferencedTableSchemaName() {
        return referencedTableSchemaName;
    }

    @DatabaseChangeProperty(
        description = "Name of the table the foreign key points to",
        exampleValue = "person")
    public String getReferencedTableName() {
        return referencedTableName;
    }

    @DatabaseChangeProperty(
        description = "Name of the column(s) the foreign key points to. Comma-separate if multiple",
        exampleValue = "id")
    public String getReferencedColumnNames() {
        return referencedColumnNames;
    }

    @DatabaseChangeProperty(description = "Name of the new foreign key constraint", exampleValue = "fk_address_person")
    public String getConstraintName() {
        return constraintName;
    }

    @DatabaseChangeProperty(description = "Defines whether the foreign key is deferrable")
    public Boolean getDeferrable() {
        return deferrable;
    }

    @DatabaseChangeProperty(description = "Defines whether the foreign key is initially deferred")
    public Boolean getInitiallyDeferred() {
        return initiallyDeferred;
    }

    /**
     * the VALIDATE keyword defines whether to check if a foreign key constraint
     * on a column in a table refers to a valid row or not.
     * @return true if ENABLE VALIDATE (this is the default), or false if ENABLE NOVALIDATE.
     */
    @DatabaseChangeProperty(description = "Defines whether to check if the foreign key constraint refers to a valid row. " +
        "This is true if the foreign key has 'ENABLE VALIDATE' set, or false if the foreign key has 'ENABLE NOVALIDATE' set.")
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

    /**
     * @deprecated Use {@link #getOnDelete()}.
     * <b>This always returns null</b> so it doesn't impact checksums when settings onDelete vs. deleteCascade
     */
    @DatabaseChangeProperty(
        description = "Deprecated. This is true to set onDelete to Cascade, priority given to onDelete tag if one exists")
    @Deprecated
    public Boolean getDeleteCascade() {
        return null;
    }

    public void setDeleteCascade(Boolean deleteCascade) {
        if ((deleteCascade != null) && deleteCascade && (this.onDelete == null)) {
            setOnDelete("CASCADE");
        } else {
            setOnDelete((String) null);
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

    @DatabaseChangeProperty(description = "ON DELETE functionality. Possible values: 'CASCADE', 'SET NULL', " +
        "'SET DEFAULT', 'RESTRICT', 'NO ACTION'",
        exampleValue = "CASCADE")
    public String getOnDelete() {
        return this.onDelete;
    }

    public void setOnDelete(String onDelete) {
        this.onDelete = onDelete;
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
        } else if (rule == ForeignKeyConstraintType.importedKeySetNull) {
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
        if (checkDefferable(database) && getDeferrable() != null) {
            deferrable = getDeferrable();
        }

        boolean initiallyDeferred = false;
        if (checkDefferable(database) && getInitiallyDeferred() != null) {
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

    private boolean checkDefferable(Database database) {
        if(!database.supportsInitiallyDeferrableColumns() && !database.failOnDefferable()){
            return false;
        }
        return true;
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
    @DatabaseChangeProperty(description = "Deprecated")
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
