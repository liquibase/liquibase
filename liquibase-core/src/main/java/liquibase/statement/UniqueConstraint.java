package liquibase.statement;

import liquibase.AbstractExtensibleObject;
import liquibase.structure.core.Index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Describes unique constraint settings of a table, used in {@link liquibase.statement.Statement} objects.
 */
public class UniqueConstraint extends AbstractExtensibleObject implements ColumnConstraint {
    private static final String CONSTRAINT_NAME = "constraintName";
    private static final String COLUMNS = "columns";

    public UniqueConstraint() {
        setAttribute(COLUMNS, new ArrayList<String>());
    }

    public UniqueConstraint(String constraintName) {
        this();
        setAttribute(CONSTRAINT_NAME, constraintName);
    }

    public UniqueConstraint addColumns(String... columns) {
        if (columns != null) {
            getAttribute(COLUMNS, List.class).addAll(Arrays.asList(columns));
        }

        return this;
    }

    public String getConstraintName() {
        return getAttribute(CONSTRAINT_NAME, String.class);
    }

    public UniqueConstraint setConstraintName(String constraintName) {
        return (UniqueConstraint) setAttribute(CONSTRAINT_NAME, constraintName);
    }

    public List<String> getColumns() {
        return Collections.unmodifiableList(getAttribute(COLUMNS, List.class));
    }
}
