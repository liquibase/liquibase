package liquibase.statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PrimaryKeyConstraint implements ColumnConstraint {

    private String constraintName;
    private String tablespace;

    private List<String> columns = new ArrayList<>();

    public PrimaryKeyConstraint() {
    }

    public PrimaryKeyConstraint(String constraintName) {
        this.constraintName = constraintName;
    }


    public String getConstraintName() {
        return constraintName;
    }

    public String getTablespace() {
        return tablespace;
    }

    public PrimaryKeyConstraint setTablespace(String tablespace) {
        this.tablespace = tablespace;
        return this;
    }

    public List<String> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    public PrimaryKeyConstraint addColumns(String... columns) {
        this.columns.addAll(Arrays.asList(columns));

        return this;
    }
}
