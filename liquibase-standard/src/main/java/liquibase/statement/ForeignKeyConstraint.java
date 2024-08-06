package liquibase.statement;

import lombok.Getter;

public class ForeignKeyConstraint implements ColumnConstraint {
    @Getter
    private final String foreignKeyName;
    @Getter
    private String column;
    @Getter
    private final String references;
    @Getter
    private String referencedTableCatalogName;
    @Getter
    private String referencedTableSchemaName;
    @Getter
    private String referencedTableName;
    @Getter
    private String referencedColumnNames;
    @Getter
    private boolean deleteCascade;
    @Getter
    private boolean initiallyDeferred;
    @Getter
    private boolean deferrable;
    /**
     * Default value is true
     */
    private boolean validateForeignKey = true;

    public ForeignKeyConstraint(String foreignKeyName,String references) {
        this.foreignKeyName = foreignKeyName;
        this.references = references;
    }

    public ForeignKeyConstraint(String foreignKeyName,String references, boolean validateForeignKey) {
        this.foreignKeyName = foreignKeyName;
        this.references = references;
        setValidateForeignKey(validateForeignKey);

    }

    public ForeignKeyConstraint(String foreignKeyName,String references, String referencedTableName, String referencedColumnNames) {
        this.foreignKeyName = foreignKeyName;
        this.references = references;
        this.referencedTableName = referencedTableName;
        this.referencedColumnNames = referencedColumnNames;
    }

    public ForeignKeyConstraint(String foreignKeyName,String references, String referencedTableName,
        String referencedColumnNames, boolean validateForeignKey) {
        this.foreignKeyName = foreignKeyName;
        this.references = references;
        this.referencedTableName = referencedTableName;
        this.referencedColumnNames = referencedColumnNames;
        setValidateForeignKey(validateForeignKey);
    }

    public ForeignKeyConstraint setColumn(String column) {
        this.column = column;
        return this;
    }


    public void setReferencedTableCatalogName(String referencedTableCatalogName) {
        this.referencedTableCatalogName = referencedTableCatalogName;
    }

    public void setReferencedTableSchemaName(String referencedTableSchemaName) {
        this.referencedTableSchemaName = referencedTableSchemaName;
    }

    public ForeignKeyConstraint setDeleteCascade(boolean deleteCascade) {
        this.deleteCascade = deleteCascade;
        return this;
    }

    public ForeignKeyConstraint setInitiallyDeferred(boolean initiallyDeferred) {
        this.initiallyDeferred = initiallyDeferred;
        return this;
    }

    public ForeignKeyConstraint setDeferrable(boolean deferrable) {
        this.deferrable = deferrable;
        return this;
    }

    public boolean shouldValidateForeignKey() {
        return validateForeignKey;
    }

    public void setValidateForeignKey(boolean validateForeignKey) {
        this.validateForeignKey = validateForeignKey;
    }
}
