package liquibase.statement;

import liquibase.structure.core.Index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UniqueConstraint implements ColumnConstraint {
    private String constraintName;
    private List<String> columns = new ArrayList<>();

    private Index backingIndex;

    public UniqueConstraint() {

    }

    public UniqueConstraint(String constraintName) {
        this.constraintName = constraintName;
    }

    public UniqueConstraint addColumns(String... columns) {
        this.columns.addAll(Arrays.asList(columns));

        return this;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }

    public List<String> getColumns() {
        return columns;
    }
}
