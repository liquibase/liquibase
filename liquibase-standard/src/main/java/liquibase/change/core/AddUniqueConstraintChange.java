package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddUniqueConstraintStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.UniqueConstraint;
import lombok.Setter;

/**
 * Adds a unique constraint to an existing column.
 */
@DatabaseChange(name = "addUniqueConstraint",
    description = "Adds a unique constraint to an existing column or set of columns.",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "column")
public class AddUniqueConstraintChange extends AbstractChange {

    @Setter
    private String catalogName;
    @Setter
    private String schemaName;
    @Setter
    private String tableName;
    @Setter
    private String columnNames;
    @Setter
    private String constraintName;
    @Setter
    private String tablespace;

    @Setter
    private Boolean clustered;
    private Boolean shouldValidate;

    @Setter
    private String forIndexName;
    @Setter
    private String forIndexSchemaName;
    @Setter
    private String forIndexCatalogName;

    @Setter
    private Boolean deferrable;
    @Setter
    private Boolean initiallyDeferred;
    @Setter
    private Boolean disabled;

    @DatabaseChangeProperty(mustEqualExisting ="column.relation.catalog", since = "3.0",
        description = "Name of the database catalog")
    public String getCatalogName() {
        return catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="column.relation.schema",
        description = "Name of the database schema")
    public String getSchemaName() {
        return schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column.relation",
        description = "Name of the table to create the unique constraint on")
    public String getTableName() {
        return tableName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column",
        description = "Name of the column(s) to create the unique constraint on. Comma separated if multiple")
    public String getColumnNames() {
        return columnNames;
    }

    @DatabaseChangeProperty(description = "Name of the unique constraint")
    public String getConstraintName() {
        return constraintName;
    }

    @DatabaseChangeProperty(description = "Tablespace to create the index in. Corresponds to file group in mssql")
    public String getTablespace() {
        return tablespace;
    }

    @DatabaseChangeProperty(description = "Defines whether the constraint is deferrable")
    public Boolean getDeferrable() {
        return deferrable;
    }

    @DatabaseChangeProperty(description = "Defines whether the constraint is initially deferred")
    public Boolean getInitiallyDeferred() {
        return initiallyDeferred;
    }

    @DatabaseChangeProperty(description = "Specifies whether the constraint is disabled")
    public Boolean getDisabled() {
        return disabled;
    }

    /**
     * In Oracle PL/SQL, the VALIDATE keyword defines whether a newly added unique constraint on a 
     * column in a table should cause existing rows to be checked to see if they satisfy the 
     * uniqueness constraint or not. 
     * @return true if ENABLE VALIDATE (this is the default), or false if ENABLE NOVALIDATE.
     */
    @DatabaseChangeProperty(description = "Defines whether to check if the unique constraint refers to a valid row. " +
        "This is true if the constraint has 'ENABLE VALIDATE' set, or false if the constraint has 'ENABLE NOVALIDATE' set.")
    public Boolean getValidate() {
        return shouldValidate;
    }

    /**
     * @param validate - if shouldValidate is set to FALSE then the constraint will be created
     * with the 'ENABLE NOVALIDATE' mode. This means the constraint would be created, but that no
     * check will be done to ensure old data has valid constraints - only new data would be checked
     * to see if it complies with the constraint logic. The default state for unique constraints is to
     * have 'ENABLE VALIDATE' set.
     */
    public void setValidate(Boolean validate) {
        this.shouldValidate = validate;
    }

    @DatabaseChangeProperty(description = "Whether to create a clustered index")
    public Boolean getClustered() {
        return clustered;
    }

    @DatabaseChangeProperty(description = "Name of the index to associate with the constraint")
    public String getForIndexName() {
        return forIndexName;
    }

    @DatabaseChangeProperty(description = "Name of the schema of the index to associate with the constraint")
    public String getForIndexSchemaName() {
        return forIndexSchemaName;
    }

    @DatabaseChangeProperty(description = "Name of the catalog of the index to associate with the constraint")
    public String getForIndexCatalogName() {
        return forIndexCatalogName;
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
        
        boolean disabled = false;
        if (getDisabled() != null) {
            disabled = getDisabled();
        }

        boolean clustered = false;
        if (getClustered() != null) {
            clustered = getClustered();
        }

        boolean shouldValidate = true;
        if (getValidate() != null) {
            shouldValidate = getValidate();
        }

        AddUniqueConstraintStatement statement = createAddUniqueConstraintStatement();
        statement.setTablespace(getTablespace())
                        .setDeferrable(deferrable)
                        .setInitiallyDeferred(initiallyDeferred)
                        .setDisabled(disabled)
                        .setClustered(clustered)
                        .setShouldValidate(shouldValidate);

        statement.setForIndexName(getForIndexName());
        statement.setForIndexSchemaName(getForIndexSchemaName());
        statement.setForIndexCatalogName(getForIndexCatalogName());

        return new SqlStatement[] { statement };
    }

    protected AddUniqueConstraintStatement createAddUniqueConstraintStatement() {
        return new AddUniqueConstraintStatement(getCatalogName(), getSchemaName(), getTableName(),
            ColumnConfig.arrayFromNames(getColumnNames()), getConstraintName());
    }


    @Override
    public ChangeStatus checkStatus(Database database) {
        ChangeStatus result = new ChangeStatus();
        try {
            UniqueConstraint example = new UniqueConstraint(getConstraintName(), getCatalogName(), getSchemaName(),
                getTableName(), Column.arrayFromNames(getColumnNames()));

            UniqueConstraint snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(example, database);
            result.assertComplete(snapshot != null, "Unique constraint does not exist");

            return result;

        } catch (Exception e) {
            return result.unknown(e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "Unique constraint added to "+getTableName()+"("+getColumnNames()+")";
    }

    @Override
    protected Change[] createInverses() {
        DropUniqueConstraintChange inverse = new DropUniqueConstraintChange();
        inverse.setSchemaName(getSchemaName());
        inverse.setTableName(getTableName());
        inverse.setConstraintName(getConstraintName());
        inverse.setUniqueColumns(getColumnNames());

        return new Change[]{
                inverse,
        };
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
