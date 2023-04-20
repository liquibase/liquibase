package liquibase.statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UniqueConstraint implements ColumnConstraint {
    private String constraintName;
    /**
     * Default value is true
     */
    private boolean validateUnique = true;
    private List<String> columns = new ArrayList<>();

    public UniqueConstraint() {

    }

    public UniqueConstraint(String constraintName) {
        this.constraintName = constraintName;
    }

    public UniqueConstraint(String constraintName, boolean validateUnique) {
        this.constraintName = constraintName;
        setValidateUnique(validateUnique);
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

    public boolean shouldValidateUnique() {
        return validateUnique;
    }

    public void setValidateUnique(boolean validateUnique) {
        this.validateUnique = validateUnique;
    }
}
