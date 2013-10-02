package liquibase.statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PrimaryKeyConstraint implements ColumnConstraint {

    private String constraintName;

	// used for PK's index configuration
	private String tablespace;
    
    private List<String> columns = new ArrayList<String>();

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

	public void setTablespace(String tablespace) {
		this.tablespace = tablespace;
	}

	public List<String> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    public PrimaryKeyConstraint addColumns(String... columns) {
        this.columns.addAll(Arrays.asList(columns));

        return this;
    }
}
