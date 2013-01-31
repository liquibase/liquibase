package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.statement.SqlStatement;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.core.AddDefaultValueStatement;
import liquibase.util.ISODateFormat;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * Sets a new default value to an existing column.
 */
@DatabaseChange(name="addDefaultValue", description = "Add Default Value", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
public class AddDefaultValueChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private String columnDataType;
    private String defaultValue;
    private String defaultValueNumeric;
    private String defaultValueDate;
    private Boolean defaultValueBoolean;
    private DatabaseFunction defaultValueComputed;
    private SequenceNextValueFunction defaultValueSequenceNext;

    @DatabaseChangeProperty(mustApplyTo ="column.relation.catalog")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustApplyTo ="column.relation.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(requiredForDatabase = "all", mustApplyTo = "column.relation")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(requiredForDatabase = "all", mustApplyTo = "column")
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

    public DatabaseFunction getDefaultValueComputed() {
        return defaultValueComputed;
    }

    public void setDefaultValueComputed(DatabaseFunction defaultValueComputed) {
        this.defaultValueComputed = defaultValueComputed;
    }

    public SequenceNextValueFunction getDefaultValueSequenceNext() {
        return defaultValueSequenceNext;
    }

    public void setDefaultValueSequenceNext(SequenceNextValueFunction defaultValueSequenceNext) {
        this.defaultValueSequenceNext = defaultValueSequenceNext;
    }

    public SqlStatement[] generateStatements(Database database) {
        Object defaultValue = null;

        if (getDefaultValue() != null) {
            defaultValue = getDefaultValue();
        } else if (getDefaultValueBoolean() != null) {
            defaultValue = getDefaultValueBoolean();
        } else if (getDefaultValueNumeric() != null) {
            try {
                defaultValue = NumberFormat.getInstance(Locale.US).parse(getDefaultValueNumeric());
            } catch (ParseException e) {
            	defaultValue = new DatabaseFunction(getDefaultValueNumeric());
            }
        } else if (getDefaultValueDate() != null) {
            try {
                defaultValue = new ISODateFormat().parse(getDefaultValueDate());
            } catch (ParseException e) {
                defaultValue = new DatabaseFunction(getDefaultValueDate());
            }
        } else if (getDefaultValueComputed() != null) {
            defaultValue = getDefaultValueComputed();
        } else if (getDefaultValueSequenceNext() != null) {
            defaultValue = getDefaultValueSequenceNext();
        }
        
        return new SqlStatement[]{
                new AddDefaultValueStatement(getCatalogName(), getSchemaName(), getTableName(), getColumnName(), getColumnDataType(), defaultValue)
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
