package liquibase.statement.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import liquibase.statement.AbstractSqlStatement;
import liquibase.statement.AutoIncrementConstraint;
import liquibase.statement.ColumnConstraint;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.PrimaryKeyConstraint;
import liquibase.statement.UniqueConstraint;

public class AddColumnStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private String columnType;
    private Object defaultValue;
    private String remarks;
    private String addAfterColumn;
    private String addBeforeColumn;
    private Integer addAtPosition;
    private Set<ColumnConstraint> constraints = new HashSet<ColumnConstraint>();

    private List<AddColumnStatement> columns = new ArrayList<AddColumnStatement>();

    public AddColumnStatement(String catalogName, String schemaName, String tableName, String columnName, String columnType, Object defaultValue, ColumnConstraint... constraints) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnName = columnName;
        this.columnType = columnType;
        this.defaultValue = defaultValue;
        if (constraints != null) {
            this.constraints.addAll(Arrays.asList(constraints));
        }
    }

    public AddColumnStatement(String catalogName, String schemaName, String tableName, String columnName, String columnType, Object defaultValue, String remarks,ColumnConstraint... constraints) {
        this(catalogName,schemaName,tableName,columnName,columnType,defaultValue,constraints);
        this.remarks = remarks;
    }

    public AddColumnStatement(List<AddColumnStatement> columns) {
        this.columns.addAll(columns);
    }

    public AddColumnStatement(AddColumnStatement... columns) {
        this(Arrays.asList(columns));
    }


    public boolean isMultiple() {
        return !columns.isEmpty();
    }

    public List<AddColumnStatement> getColumns() {
        return columns;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public String getRemarks() {
        return remarks;
    }

    public Set<ColumnConstraint> getConstraints() {
        return constraints;
    }

    public boolean isAutoIncrement() {
        for (ColumnConstraint constraint : getConstraints()) {
            if (constraint instanceof AutoIncrementConstraint) {
                return true;
            }
        }
        return false;
    }

    public AutoIncrementConstraint getAutoIncrementConstraint() {
        AutoIncrementConstraint autoIncrementConstraint = null;
        
        for (ColumnConstraint constraint : getConstraints()) {
            if (constraint instanceof AutoIncrementConstraint) {
                autoIncrementConstraint = (AutoIncrementConstraint) constraint;
                break;
            }
        }
        
        return autoIncrementConstraint;
    }

    public boolean isPrimaryKey() {
        for (ColumnConstraint constraint : getConstraints()) {
            if (constraint instanceof PrimaryKeyConstraint) {
                return true;
            }
        }
        return false;
    }

    public boolean isNullable() {
        if (isPrimaryKey()) {
            return false;
        }
        for (ColumnConstraint constraint : getConstraints()) {
            if (constraint instanceof NotNullConstraint) {
                return false;
            }
        }
        return true;
    }

    public boolean isUnique() {
        for (ColumnConstraint constraint : getConstraints()) {
            if (constraint instanceof UniqueConstraint) {
                return true;
            }
        }
        return false;
    }

    public String getUniqueStatementName() {
        for (ColumnConstraint constraint : getConstraints()) {
            if (constraint instanceof UniqueConstraint) {
                return ((UniqueConstraint) constraint).getConstraintName();
            }
        }
        return null;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public String getAddAfterColumn() {
    	return addAfterColumn;
    }

    public void setAddAfterColumn(String addAfterColumn) {
		this.addAfterColumn = addAfterColumn;
	}

    public String getAddBeforeColumn() {
    	return addBeforeColumn;
    }

    public void setAddBeforeColumn(String addBeforeColumn) {
		this.addBeforeColumn = addBeforeColumn;
	}

	public Integer getAddAtPosition() {
		return addAtPosition;
	}

	public void setAddAtPosition(Integer addAtPosition) {
		this.addAtPosition = addAtPosition;
	}
}
