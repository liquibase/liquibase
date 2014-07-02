package liquibase.statement;

import liquibase.AbstractExtensibleObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PrimaryKeyConstraint extends AbstractExtensibleObject implements Constraint {

    private static final String CONSTRAINT_NAME = "constraintName";
	private static final String TABLESPACE = "tablespace";
    private static final String COLUMNS = "columns";

    public PrimaryKeyConstraint() {
        setAttribute(COLUMNS, new ArrayList<String>());
    }

    public PrimaryKeyConstraint(String constraintName) {
        this();
        setAttribute(CONSTRAINT_NAME, constraintName);
    }


    public String getConstraintName() {
        return getAttribute(CONSTRAINT_NAME, String.class);
    }

    public PrimaryKeyConstraint setConstraintName(String constraintName) {
        return (PrimaryKeyConstraint) setAttribute(CONSTRAINT_NAME, constraintName);
    }

	public String getTablespace() {
		return getAttribute(TABLESPACE, String.class);
	}

	public PrimaryKeyConstraint setTablespace(String tablespace) {
		return (PrimaryKeyConstraint) setAttribute(TABLESPACE, tablespace);
	}

	public List<String> getColumns() {
        return Collections.unmodifiableList(getAttribute(COLUMNS, List.class));
    }

    public PrimaryKeyConstraint addColumns(String... columns) {
        if (columns != null) {
            getAttribute(COLUMNS, List.class).addAll(Arrays.asList(columns));
        }
        return this;
    }
}
