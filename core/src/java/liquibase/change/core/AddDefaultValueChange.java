package liquibase.change.core;

import liquibase.database.Database;
import liquibase.statement.AddDefaultValueStatement;
import liquibase.statement.ComputedDateValue;
import liquibase.statement.ComputedNumericValue;
import liquibase.statement.SqlStatement;
import liquibase.util.ISODateFormat;
import liquibase.util.StringUtils;
import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.Change;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * Sets a new default value to an existing column.
 */
public class AddDefaultValueChange extends AbstractChange {

    private String schemaName;
    private String tableName;
    private String columnName;
    private String columnDataType;
    private String defaultValue;
    private String defaultValueNumeric;
    private String defaultValueDate;
    private Boolean defaultValueBoolean;

    public AddDefaultValueChange() {
        super("addDefaultValue", "Add Default Value", ChangeMetaData.PRIORITY_DEFAULT);
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
    
    public String getColumnDataType() {
		return columnDataType;
	}
    
    public void setColumnDataType(String columnDataType) {
		this.columnDataType = columnDataType;
	}

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }


    public String getDefaultValueNumeric() {
        return defaultValueNumeric;
    }

    public void setDefaultValueNumeric(String defaultValueNumeric) {
        this.defaultValueNumeric = defaultValueNumeric;
    }

    public String getDefaultValueDate() {
        return defaultValueDate;
    }

    public void setDefaultValueDate(String defaultValueDate) {
        this.defaultValueDate = defaultValueDate;
    }


    public Boolean getDefaultValueBoolean() {
        return defaultValueBoolean;
    }

    public void setDefaultValueBoolean(Boolean defaultValueBoolean) {
        this.defaultValueBoolean = defaultValueBoolean;
    }

    public SqlStatement[] generateStatements(Database database) {
        Object defaultValue = null;

        if (getDefaultValue() != null) {
            defaultValue = getDefaultValue();
        } else if (getDefaultValueBoolean() != null) {
            defaultValue = Boolean.valueOf(getDefaultValueBoolean());
        } else if (getDefaultValueNumeric() != null) {
            try {
                defaultValue = NumberFormat.getInstance(Locale.US).
                	parse(getDefaultValueNumeric()); 
            } catch (ParseException e) {
            	defaultValue = new ComputedNumericValue(getDefaultValueNumeric());
            }
        } else if (getDefaultValueDate() != null) {
            try {
                defaultValue = new ISODateFormat().parse(getDefaultValueDate());
            } catch (ParseException e) {
                defaultValue = new ComputedDateValue(getDefaultValueDate());
            }
        }
        
        return new SqlStatement[]{
                new AddDefaultValueStatement(getSchemaName(), getTableName(), getColumnName(), getColumnDataType(), defaultValue)
        };
    }

    @Override
    protected Change[] createInverses() {
        DropDefaultValueChange inverse = new DropDefaultValueChange();
        inverse.setSchemaName(getSchemaName());
        inverse.setTableName(getTableName());
        inverse.setColumnName(getColumnName());
        inverse.setColumnDataType(getColumnDataType());

        return new Change[]{
                inverse
        };
    }

    public String getConfirmationMessage() {
        return "Default value added to " + getTableName() + "." + getColumnName();
    }
}
