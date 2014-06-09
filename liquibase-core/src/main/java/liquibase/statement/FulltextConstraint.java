package liquibase.statement;

import liquibase.structure.core.Index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FulltextConstraint implements ColumnConstraint {
    private String constraintName;
    private List<String> columns = new ArrayList<String>();

    private Index backingIndex;

    public FulltextConstraint() {

    }

    public FulltextConstraint(String constraintName) {
        this.constraintName = constraintName;
    }

    public FulltextConstraint addColumns(String... columns) {
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
