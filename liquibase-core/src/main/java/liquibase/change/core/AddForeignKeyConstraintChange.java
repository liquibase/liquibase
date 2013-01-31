package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.structure.core.Column;
import liquibase.structure.core.ForeignKeyConstraintType;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddForeignKeyConstraintStatement;

/**
 * Adds a foreign key constraint to an existing column.
 */
 @DatabaseChange(name="addForeignKeyConstraint", description = "Add Foreign Key Constraint", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
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

    private String onUpdate;
    private String onDelete;

	// Some databases supports creation of FK with references to column marked as unique, not primary
	// If FK referenced to such unique column this option should be set to true
	private Boolean referencesUniqueColumn;

    @DatabaseChangeProperty(mustEqualExisting ="column.relation.catalog")
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

    @DatabaseChangeProperty(requiredForDatabase = "all", mustEqualExisting = "column.relation")
    public String getBaseTableName() {
        return baseTableName;
    }

    public void setBaseTableName(String baseTableName) {
        this.baseTableName = baseTableName;
    }

    @DatabaseChangeProperty(requiredForDatabase = "all", mustEqualExisting = "column")
    public String getBaseColumnNames() {
        return baseColumnNames;
    }

    public void setBaseColumnNames(String baseColumnNames) {
        this.baseColumnNames = baseColumnNames;
    }

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

    @DatabaseChangeProperty(requiredForDatabase = "all")
    public String getReferencedTableName() {
        return referencedTableName;
    }

    public void setReferencedTableName(String referencedTableName) {
        this.referencedTableName = referencedTableName;
    }

    @DatabaseChangeProperty(requiredForDatabase = "all")
    public String getReferencedColumnNames() {
        return referencedColumnNames;
    }

    public void setReferencedColumnNames(String referencedColumnNames) {
        this.referencedColumnNames = referencedColumnNames;
    }

    @DatabaseChangeProperty(requiredForDatabase = "all")
    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }

    public Boolean getDeferrable() {
        return deferrable;
    }

    public void setDeferrable(Boolean deferrable) {
        this.deferrable = deferrable;
    }

    public Boolean getInitiallyDeferred() {
        return initiallyDeferred;
    }

    public void setInitiallyDeferred(Boolean initiallyDeferred) {
        this.initiallyDeferred = initiallyDeferred;
    }

//    public Boolean getDeleteCascade() {
//        return deleteCascade;
//    }

    public void setDeleteCascade(Boolean deleteCascade) {
        if (deleteCascade != null && deleteCascade) {
            setOnDelete("CASCADE");
        }
    }

    public void setOnUpdate(String rule) {
        this.onUpdate = rule;
    }

    public String getOnUpdate() {
        return onUpdate;
    }

    public void setOnDelete(String onDelete) {
        this.onDelete = onDelete;
    }

    public String getOnDelete() {
        return this.onDelete;
    }

	public Boolean getReferencesUniqueColumn() {
		return referencesUniqueColumn;
	}

	public void setReferencesUniqueColumn(Boolean referencesUniqueColumn) {
		this.referencesUniqueColumn = referencesUniqueColumn;
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

    public SqlStatement[] generateStatements(Database database) {

        boolean deferrable = false;
        if (getDeferrable() != null) {
            deferrable = getDeferrable();
        }

        boolean initiallyDeferred = false;
        if (getInitiallyDeferred() != null) {
            initiallyDeferred = getInitiallyDeferred();
        }

        return new SqlStatement[]{
                new AddForeignKeyConstraintStatement(getConstraintName(),
                        getBaseTableCatalogName(),
                        getBaseTableSchemaName(),
                        getBaseTableName(),
                        getBaseColumnNames(),
                        getReferencedTableCatalogName(),
                        getReferencedTableSchemaName(),
                        getReferencedTableName(),
                        getReferencedColumnNames())
                        .setDeferrable(deferrable)
                        .setInitiallyDeferred(initiallyDeferred)
                        .setOnUpdate(getOnUpdate())
                        .setOnDelete(getOnDelete())
		                .setReferencesUniqueColumn(getReferencesUniqueColumn())
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

    public String getConfirmationMessage() {
        return "Foreign key contraint added to " + getBaseTableName() + " (" + getBaseColumnNames() + ")";
    }
}
