package liquibase.statement;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PrimaryKeyConstraint implements ColumnConstraint {

    @Getter
    private String constraintName;
    @Getter
    private boolean initiallyDeferred;
    @Getter
    private boolean deferrable;

    /**
     * Default value is true
     */
    @Setter
    private boolean validatePrimaryKey = true;

    // used for PK's index configuration
    @Getter
    private String tablespace;

    private final List<String> columns = new ArrayList<>();

    public PrimaryKeyConstraint() {
    }

    public PrimaryKeyConstraint(String constraintName) {
        this.constraintName = constraintName;
    }

    public PrimaryKeyConstraint(String constraintName, boolean validatePrimaryKey) {
        this.constraintName = constraintName;
        setValidatePrimaryKey(validatePrimaryKey);
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

    public boolean shouldValidatePrimaryKey() {
        return validatePrimaryKey;
    }

    public PrimaryKeyConstraint setInitiallyDeferred(boolean initiallyDeferred) {
        this.initiallyDeferred = initiallyDeferred;
        return this;
    }

    public PrimaryKeyConstraint setDeferrable(boolean deferrable) {
        this.deferrable = deferrable;
        return this;
    }

}
