package liquibase.statement;

import liquibase.AbstractExtensibleObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Describes the foreign key constraints on a column, used in {@link liquibase.statement.Statement} objects.
 */
public class ForeignKeyConstraint extends AbstractExtensibleObject implements Constraint {

    private static final String FOREIGN_KEY_NAME = "foreignKeyName";
    private static final String COLUMN_NAMES = "columnNames";
    private static final String REFERENCES = "references";
    private static final String REFERENCED_TABLE_NAME = "referencedTableName";
    private static final String REFERENCED_COLUMN_NAMES = "referencedColumnNames";
    private static final String DELETE_CASCADE = "deleteCascade";
    private static final String INITIALLY_DEFERRED = "initiallyDeferred";
    private static final String DEFERRABLE = "deferrable";

    public ForeignKeyConstraint() {
    }

    public ForeignKeyConstraint(String foreignKeyName,String references) {
        setAttribute(FOREIGN_KEY_NAME, foreignKeyName);
        setAttribute(REFERENCES, references);
    }

    public ForeignKeyConstraint(String foreignKeyName,String references, String referencedTableName, String referencedColumnNames) {
        setAttribute(FOREIGN_KEY_NAME, foreignKeyName);
        setAttribute(REFERENCES, references);
        setAttribute(REFERENCED_TABLE_NAME, referencedTableName);
        setAttribute(REFERENCED_COLUMN_NAMES, referencedColumnNames);
    }

    public String getForeignKeyName() {
        return getAttribute(FOREIGN_KEY_NAME, String.class);
    }

    public ForeignKeyConstraint setForeignKeyName(String foreignKeyName) {
        return (ForeignKeyConstraint) setAttribute(FOREIGN_KEY_NAME, foreignKeyName);
    }

    public List<String> getColumnNames() {
        return Collections.unmodifiableList(getAttribute(COLUMN_NAMES, List.class));
    }

    public ForeignKeyConstraint addColumns(String... columns) {
        if (columns != null) {
            getAttribute(COLUMN_NAMES, List.class).addAll(Arrays.asList(columns));
        }

        return this;
    }

    public String getReferences() {
        return getAttribute(REFERENCES, String.class);
    }

    public ForeignKeyConstraint setReferences(String references) {
        return (ForeignKeyConstraint) setAttribute(REFERENCES, references);
    }

    public boolean getDeleteCascade() {
        return getAttribute(DELETE_CASCADE, false);
    }

    public String getReferencedTableName() {
        return getAttribute(REFERENCED_TABLE_NAME, String.class);
    }

    public ForeignKeyConstraint setReferencedTableName(String referencedTableName) {
        return (ForeignKeyConstraint) setAttribute(REFERENCED_TABLE_NAME, referencedTableName);
    }


    public String getReferencedColumnNames() {
        return getAttribute(REFERENCED_COLUMN_NAMES, String.class);
    }

    public ForeignKeyConstraint setReferencedColumnNames(String referencedColumnNames) {
        return (ForeignKeyConstraint) setAttribute(REFERENCED_COLUMN_NAMES, referencedColumnNames);
    }

    public ForeignKeyConstraint setDeleteCascade(boolean deleteCascade) {
        return (ForeignKeyConstraint) setAttribute(DELETE_CASCADE, deleteCascade);
    }

    public boolean getInitiallyDeferred() {
        return getAttribute(INITIALLY_DEFERRED, false);
    }

    public ForeignKeyConstraint setInitiallyDeferred(boolean initiallyDeferred) {
        return (ForeignKeyConstraint) setAttribute(INITIALLY_DEFERRED, initiallyDeferred);
    }

    public boolean getDeferrable() {
        return getAttribute(DEFERRABLE, false);
    }

    public ForeignKeyConstraint setDeferrable(boolean deferrable) {
        return (ForeignKeyConstraint) setAttribute(DEFERRABLE, deferrable);
    }
}
