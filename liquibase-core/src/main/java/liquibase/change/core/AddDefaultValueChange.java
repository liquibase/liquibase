package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddDefaultValueStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.util.ISODateFormat;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

/**
 * Sets a new default value to an existing column.
 */
@DatabaseChange(name = "addDefaultValue",
        description = "Adds a default value to the database definition for the specified column.\n" +
                "One of defaultValue, defaultValueNumeric, defaultValueBoolean or defaultValueDate must be set",
        priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
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

    private String defaultValueConstraintName;

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validate = new ValidationErrors();

        int nonNullValues = 0;
        if (defaultValue != null) {
            nonNullValues++;
        }
        if (defaultValueNumeric != null) {
            nonNullValues++;
        }
        if (defaultValueBoolean != null) {
            nonNullValues++;
        }
        if (defaultValueDate != null) {
            nonNullValues++;
        }
        if (defaultValueComputed != null) {
            nonNullValues++;
        }
        if (defaultValueSequenceNext != null) {
            nonNullValues++;
        }

        if (nonNullValues > 1) {
            validate.addError("Only one defaultValue* value can be specified");
        } else {
            validate.addAll(super.validate(database));
        }

        return validate;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column.relation.catalog", since = "3.0")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column.relation.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column.relation", description = "Name of the table to containing the column", exampleValue = "file")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column", description = "Name of the column to add a default value to", exampleValue = "fileName")
    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    @DatabaseChangeProperty(description = "Current data type of the column to add default value to", exampleValue = "varchar(50)")
    public String getColumnDataType() {
        return columnDataType;
    }

    public void setColumnDataType(String columnDataType) {
        this.columnDataType = columnDataType;
    }

    @DatabaseChangeProperty(description = "Default value. Either this property or one of the other defaultValue* properties are required.", exampleValue = "Something Else", requiredForDatabase = "none")
    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }


    @DatabaseChangeProperty(requiredForDatabase = "none", exampleValue = "439.2")
    public String getDefaultValueNumeric() {
        return defaultValueNumeric;
    }

    public void setDefaultValueNumeric(String defaultValueNumeric) {
        this.defaultValueNumeric = defaultValueNumeric;
    }

    @DatabaseChangeProperty(requiredForDatabase = "none", exampleValue = "2008-02-12T12:34:03")
    public String getDefaultValueDate() {
        return defaultValueDate;
    }

    public void setDefaultValueDate(String defaultValueDate) {
        this.defaultValueDate = defaultValueDate;
    }


    @DatabaseChangeProperty(requiredForDatabase = "none")
    public Boolean getDefaultValueBoolean() {
        return defaultValueBoolean;
    }

    public void setDefaultValueBoolean(Boolean defaultValueBoolean) {
        this.defaultValueBoolean = defaultValueBoolean;
    }

    @DatabaseChangeProperty(requiredForDatabase = "none")
    public DatabaseFunction getDefaultValueComputed() {
        return defaultValueComputed;
    }

    public void setDefaultValueComputed(DatabaseFunction defaultValueComputed) {
        this.defaultValueComputed = defaultValueComputed;
    }

    @DatabaseChangeProperty(requiredForDatabase = "none")
    public SequenceNextValueFunction getDefaultValueSequenceNext() {
        return defaultValueSequenceNext;
    }

    public void setDefaultValueSequenceNext(SequenceNextValueFunction defaultValueSequenceNext) {
        this.defaultValueSequenceNext = defaultValueSequenceNext;
    }

    public String getDefaultValueConstraintName() {
        return defaultValueConstraintName;
    }

    public void setDefaultValueConstraintName(String defaultValueConstraintName) {
        this.defaultValueConstraintName = defaultValueConstraintName;
    }

    @Override
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
            ((SequenceNextValueFunction) defaultValue).setSequenceSchemaName(this.getSchemaName());
        }

        AddDefaultValueStatement statement = new AddDefaultValueStatement(getCatalogName(), getSchemaName(), getTableName(), getColumnName(), getColumnDataType(), defaultValue);
        statement.setDefaultValueConstraintName(this.getDefaultValueConstraintName());

        return new SqlStatement[]{
                statement
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

    @Override
    public String getConfirmationMessage() {
        return "Default value added to " + getTableName() + "." + getColumnName();
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        ChangeStatus result = new ChangeStatus();
        try {
            Column column = SnapshotGeneratorFactory.getInstance().createSnapshot(new Column(Table.class, getCatalogName(), getSchemaName(), getTableName(), getColumnName()), database);
            if (column == null) {
                return result.unknown("Column " + getColumnName() + " does not exist");
            }

            result.assertComplete(column.getDefaultValue() != null, "Column "+getColumnName()+" has no default value");
            if (column.getDefaultValue() == null) {
                return result;
            }

            if (getDefaultValue() != null) {
                return result.assertCorrect(getDefaultValue().equals(column.getDefaultValue()), "Default value was "+column.getDefaultValue());
            } else if (getDefaultValueDate() != null) {
                return result.assertCorrect(getDefaultValueDate().equals(new ISODateFormat().format((Date) column.getDefaultValue())), "Default value was "+column.getDefaultValue());
            } else if (getDefaultValueNumeric() != null) {
                return result.assertCorrect(getDefaultValueNumeric().equals(column.getDefaultValue().toString()), "Default value was "+column.getDefaultValue());
            } else if (getDefaultValueBoolean() != null) {
                return result.assertCorrect(getDefaultValueBoolean().equals(column.getDefaultValue()), "Default value was "+column.getDefaultValue());
            } else if (getDefaultValueComputed() != null) {
                return result.assertCorrect(getDefaultValueComputed().equals(column.getDefaultValue()), "Default value was "+column.getDefaultValue());
            } else if (getDefaultValueSequenceNext() != null) {
                return result.assertCorrect(getDefaultValueSequenceNext().equals(column.getDefaultValue()), "Default value was "+column.getDefaultValue());
            } else {
                return result.unknown("Unknown default value type");
            }
        } catch (Exception e) {
            return result.unknown(e);
        }
    }
}
