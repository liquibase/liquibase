package liquibase.database.sql;

public class ForeignKeyConstraint implements ColumnConstraint {
    private String foreignKeyName;
    private String column;
    private String references;
    private boolean deleteCascade = false;
    private boolean initiallyDeferred = false;
    private boolean deferrable = false;

    public ForeignKeyConstraint(String foreignKeyName,String references) {
        this.foreignKeyName = foreignKeyName;
        this.references = references;
    }

    public String getForeignKeyName() {
        return foreignKeyName;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }


    public String getReferences() {
        return references;
    }

    public boolean isDeleteCascade() {
        return deleteCascade;
    }

    public void setDeleteCascade(boolean deleteCascade) {
        this.deleteCascade = deleteCascade;
    }

    public boolean isInitiallyDeferred() {
        return initiallyDeferred;
    }

    public void setInitiallyDeferred(boolean initiallyDeferred) {
        this.initiallyDeferred = initiallyDeferred;
    }

    public boolean isDeferrable() {
        return deferrable;
    }

    public void setDeferrable(boolean deferrable) {
        this.deferrable = deferrable;
    }
}
