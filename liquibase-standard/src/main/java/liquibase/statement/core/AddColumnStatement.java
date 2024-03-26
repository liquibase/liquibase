package liquibase.statement.core;

import liquibase.statement.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
public class AddColumnStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private String columnType;
    private Object defaultValue;
    @Setter
    private String defaultValueConstraintName;
    private String remarks;
    @Setter
    private String addAfterColumn;
    @Setter
    private String addBeforeColumn;
    @Setter
    private Integer addAtPosition;
    @Setter
    private Boolean computed;
    private final Set<ColumnConstraint> constraints = new HashSet<>();

    private final List<AddColumnStatement> columns = new ArrayList<>();

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

    public boolean shouldValidateNullable() {
        if (isPrimaryKey()) {
            return false;
        }
        for (ColumnConstraint constraint : getConstraints()) {
            if (constraint instanceof NotNullConstraint) {
                if (!((NotNullConstraint) constraint).shouldValidateNullable()) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean shouldValidateUnique() {
        for (ColumnConstraint constraint : getConstraints()) {
            if (constraint instanceof UniqueConstraint) {
                if (!((UniqueConstraint) constraint).shouldValidateUnique()) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean shouldValidatePrimaryKey() {
        for (ColumnConstraint constraint : getConstraints()) {
            if (constraint instanceof PrimaryKeyConstraint) {
                if (!((PrimaryKeyConstraint) constraint).shouldValidatePrimaryKey()) {
                    return false;
                }
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

}
