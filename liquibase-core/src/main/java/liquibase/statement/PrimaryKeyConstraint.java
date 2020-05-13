package liquibase.statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PrimaryKeyConstraint implements ColumnConstraint {

    private String constraintName;

  /**
   * Default value is true
   */
  private boolean validatePrimaryKey = true;

	// used for PK's index configuration
	private String tablespace;
    
    private List<String> columns = new ArrayList<>();

    public PrimaryKeyConstraint() {
    }

    public PrimaryKeyConstraint(String constraintName) {
        this.constraintName = constraintName;
    }

  public PrimaryKeyConstraint(String constraintName, boolean validatePrimaryKey) {
    this.constraintName = constraintName;
    setValidatePrimaryKey(validatePrimaryKey);
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

  public boolean shouldValidatePrimaryKey() {
    return validatePrimaryKey;
  }

  public void setValidatePrimaryKey(boolean validatePrimaryKey) {
    this.validatePrimaryKey = validatePrimaryKey;
  }
}
