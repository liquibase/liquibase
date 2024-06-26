package liquibase.change.core;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import liquibase.change.AbstractChange;
import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
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
import lombok.Setter;

/**
 * Sets a new default value to an existing column.
 */
@DatabaseChange(name = "addDefaultValue",
        description = "Adds a default value to the database definition for the specified column.\n" +
                "One of defaultValue, defaultValueNumeric, defaultValueBoolean or defaultValueDate must be set",
        priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
@Setter
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

    @DatabaseChangeProperty(mustEqualExisting = "column.relation.catalog", since = "3.0", description = "Name of the database catalog")
    public String getCatalogName() {
        return catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column.relation.schema", description = "Name of the database schema")
    public String getSchemaName() {
        return schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column.relation", description = "Name of the table containing the column to modify",
        exampleValue = "file")
    public String getTableName() {
        return tableName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column", description = "Name of the column to add a default value to",
        exampleValue = "fileName")
    public String getColumnName() {
        return columnName;
    }

    @DatabaseChangeProperty(description = "Current data type of the column to add a default value to", exampleValue = "varchar(50)")
    public String getColumnDataType() {
        return columnDataType;
    }

    @DatabaseChangeProperty(exampleValue = "Something Else", requiredForDatabase = "none",
        description = "Default value for fields in the column. Either this property or another defaultValue* property is required.")
    public String getDefaultValue() {
        return defaultValue;
    }

    @DatabaseChangeProperty(requiredForDatabase = "none", exampleValue = "439.2",
        description = "Default value for a column of a numeric type. For example: integer, bigint, bigdecimal, and others.")
    public String getDefaultValueNumeric() {
        return defaultValueNumeric;
    }

    @DatabaseChangeProperty(requiredForDatabase = "none", exampleValue = "2008-02-12T12:34:03",
        description = "Default date and time value for column. The value is specified in one of the following forms: " +
            "YYYY-MM-DD, hh:mm:ss, or YYYY-MM-DDThh:mm:ss.")
    public String getDefaultValueDate() {
        return defaultValueDate;
    }

    @DatabaseChangeProperty(requiredForDatabase = "none", description = "Default value for a column of a boolean type.")
    public Boolean getDefaultValueBoolean() {
        return defaultValueBoolean;
    }

    @DatabaseChangeProperty(requiredForDatabase = "none",
        description = "Default value that is returned from a function or procedure call of the same type as the column. " +
            "Contains the function or column name to call. Differs from defaultValue by returning the value of the function or column " +
            "you specify instead of the name of the function/column as a string. Can also perform operations on the returned value.")
    public DatabaseFunction getDefaultValueComputed() {
        return defaultValueComputed;
    }

    @DatabaseChangeProperty(requiredForDatabase = "none",
        description = "Sets value for a specified column by using the value of the existing sequence. " +
            "With every new input, the next value of the sequence will be taken.")
    public SequenceNextValueFunction getDefaultValueSequenceNext() {
        return defaultValueSequenceNext;
    }

    @DatabaseChangeProperty(description = "Sets a unique name for the default constraint used for a specific column. " +
        "Works only along with any of the defaultValue* properties listed.")
    public String getDefaultValueConstraintName() {
        return defaultValueConstraintName;
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
            defaultValue = new SequenceNextValueFunction(this.getSchemaName(), getDefaultValueSequenceNext().getValue());
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
