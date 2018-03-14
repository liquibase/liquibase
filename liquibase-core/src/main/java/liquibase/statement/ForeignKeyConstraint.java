package liquibase.statement;

public class ForeignKeyConstraint implements ColumnConstraint {
    private String foreignKeyName;
    private String column;
    private String references;
    private String referencedTableCatalogName;
    private String referencedTableSchemaName;
    private String referencedTableName;
    private String referencedColumnNames;
    private boolean deleteCascade = false;
    private boolean initiallyDeferred = false;
    private boolean deferrable = false;
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

    public String getForeignKeyName() {
        return foreignKeyName;
    }

    public String getColumn() {
        return column;
    }

    public ForeignKeyConstraint setColumn(String column) {
        this.column = column;
        return this;
    }


    public String getReferences() {
        return references;
    }

    public boolean isDeleteCascade() {
        return deleteCascade;
    }

    public String getReferencedTableName() {
        return referencedTableName;
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

    public String getReferencedColumnNames() {
        return referencedColumnNames;
    }

    public ForeignKeyConstraint setDeleteCascade(boolean deleteCascade) {
        this.deleteCascade = deleteCascade;
        return this;
    }

    public boolean isInitiallyDeferred() {
        return initiallyDeferred;
    }

    public ForeignKeyConstraint setInitiallyDeferred(boolean initiallyDeferred) {
        this.initiallyDeferred = initiallyDeferred;
        return this;
    }

    public boolean isDeferrable() {
        return deferrable;
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