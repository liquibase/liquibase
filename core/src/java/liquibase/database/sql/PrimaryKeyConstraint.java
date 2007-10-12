package liquibase.database.sql;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;

public class PrimaryKeyConstraint implements ColumnConstraint {

    private String constraintName;
    
    private List<String> columns = new ArrayList<String>();

    public PrimaryKeyConstraint(String constraintName) {
        this.constraintName = constraintName;
    }


    public String getConstraintName() {
        return constraintName;
    }

    public List<String> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    public PrimaryKeyConstraint addColumns(String... columns) {
        this.columns.addAll(Arrays.asList(columns));

        return this;
    }
}
