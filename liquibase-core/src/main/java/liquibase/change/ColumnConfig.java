package liquibase.change;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import liquibase.serializer.LiquibaseSerializable;
import liquibase.serializer.ReflectionSerializer;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceCurrentValueFunction;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.structure.core.Column;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;
import liquibase.structure.core.UniqueConstraint;
import liquibase.util.ISODateFormat;
import liquibase.util.StringUtils;

/**
 * The standard configuration used by Change classes to represent a column.
 * It is not required that a column-based Change uses this class, but parsers should look for it so it is a helpful convenience.
 * The definitions of "defaultValue" and "value" will vary based on the Change and may not be applicable in all cases.
 */
public class ColumnConfig implements LiquibaseSerializable {
    private String name;
    private String type;
    private String value;
    private Number valueNumeric;
    private Date valueDate;
    private Boolean valueBoolean;
    private String valueBlobFile;
    private String valueClobFile;
    private String encoding;
    private DatabaseFunction valueComputed;
    private SequenceNextValueFunction valueSequenceNext;
    private SequenceCurrentValueFunction valueSequenceCurrent;

    private String defaultValue;
    private Number defaultValueNumeric;
    private Date defaultValueDate;
    private Boolean defaultValueBoolean;
    private DatabaseFunction defaultValueComputed;
    private SequenceNextValueFunction defaultValueSequenceNext;

    private ConstraintsConfig constraints;
    private Boolean autoIncrement;
    private BigInteger startWith;
    private BigInteger incrementBy;
    private String remarks;

    /**
     * Create a ColumnConfig object based on a {@link Column} snapshot.
     * It will attempt to set as much as possible based on the information in the snapshot.
     */
    public ColumnConfig(Column columnSnapshot) {
        setName(columnSnapshot.getName());
        setType(columnSnapshot.getType().toString());

        if (columnSnapshot.getRelation() != null && columnSnapshot.getRelation() instanceof Table) {
            if (columnSnapshot.getDefaultValue() != null) {
                setDefaultValue(columnSnapshot.getDefaultValue().toString());
            }
            ConstraintsConfig constraints = new ConstraintsConfig();

            constraints.setNullable(columnSnapshot.isNullable());

            if (columnSnapshot.isAutoIncrement()) {
                setAutoIncrement(true);
                setStartWith(columnSnapshot.getAutoIncrementInformation().getStartWith());
                setIncrementBy(columnSnapshot.getAutoIncrementInformation().getIncrementBy());
            } else {
                setAutoIncrement(false);
            }


            Table table = (Table) columnSnapshot.getRelation();
            PrimaryKey primaryKey = table.getPrimaryKey();
            if (primaryKey != null && primaryKey.getColumnNamesAsList().contains(columnSnapshot.getName())) {
                constraints.setPrimaryKey(true);
                constraints.setPrimaryKeyName(primaryKey.getName());
                constraints.setPrimaryKeyTablespace(primaryKey.getTablespace());
            }

            List<UniqueConstraint> uniqueConstraints = table.getUniqueConstraints();
            if (uniqueConstraints != null) {
                for (UniqueConstraint constraint : uniqueConstraints) {
                    if (constraint.getColumnNames().contains(getName())) {
                        constraints.setUnique(true);
                        constraints.setUniqueConstraintName(constraint.getName());
                    }
                }
            }

            List<ForeignKey> fks = table.getOutgoingForeignKeys();
            if (fks != null) {
                for (ForeignKey fk : fks) {
                    if (fk.getForeignKeyColumns().equals(getName())) {
                        constraints.setForeignKeyName(fk.getName());
                        constraints.setReferences(fk.getPrimaryKeyTable().getName() + "(" + fk.getPrimaryKeyColumns() + ")");
                    }
                }
            }

            if (constraints.isPrimaryKey() == null) {
                constraints.setPrimaryKey(false);
            }
            if (constraints.isUnique() == null) {
                constraints.setUnique(false);
            }
            setConstraints(constraints);
        }

        setRemarks(columnSnapshot.getRemarks());
    }

    /**
     * Create am empty ColumnConfig object. Boolean and other object values will default to null.
     */
    public ColumnConfig() {
    }


    /**
     * The name of the column.
     */
    public String getName() {
        return name;
    }

    public ColumnConfig setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * The data type fof the column.
     * This value will pass through {@link liquibase.datatype.DataTypeFactory#fromDescription(String)} before being included in SQL.
     */
    public String getType() {
        return type;
    }

    public ColumnConfig setType(String type) {
        this.type = type;
        return this;
    }

    /**
     * The String value to set this column to. If you do not want the value set by {@link #setValue(String)}
     * use a more specific function like {@link #getValueNumeric()} or the more generic {@link #getValueObject()}
     * <p></p>
     * If performing an data manipulation operation, the setValue* functions should be used to set what the columns should be set to.
     * If performing a data definition operation, this setValue* functions should be used to set what existing rows should be set to (may be different than the default value for new rows)
     */
    public String getValue() {
        return value;
    }


    /**
     * Sets the string value this column should be set to.
     * If you are trying to set a value type other than a string, use the more specific functions like {@link #setValueNumeric(Number)}.
     * This method does no processing of the string. Any trimming is expected to be done beforehand. It does not conver the string "null" to null
     * so that you can set the string "null" as a value if you are feeling particularly crazy.
     */
    public ColumnConfig setValue(String value) {
        this.value = value;

        return this;
    }

    /**
     * Return the numeric value this column should be set to.
     * @see #setValue(String)
     */
    public Number getValueNumeric() {
        return valueNumeric;
    }


    /**
     * Set the number this column should be set to. Supports integers and decimals, and strips off any wrapping parentheses.
     * If the passed value cannot be parsed as a number, it is assumed to be a function that returns a number.
     * If the value "null" is passed, it will set a null value.
     */
    public ColumnConfig setValueNumeric(String valueNumeric) {
        if (valueNumeric == null || valueNumeric.equalsIgnoreCase("null")) {
            this.valueNumeric = null;
        } else {
            if (valueNumeric.startsWith("(")) {
                valueNumeric = valueNumeric.replaceFirst("^\\(", "");
                valueNumeric = valueNumeric.replaceFirst("\\)$", "");
            }

            try {
                this.valueNumeric = NumberFormat.getInstance(Locale.US).parse(valueNumeric);
            } catch (ParseException e) {
                this.valueComputed = new DatabaseFunction(valueNumeric);
            }
        }

        return this;
    }

    public ColumnConfig setValueNumeric(Number valueNumeric) {
        this.valueNumeric = valueNumeric;

        return this;
    }

    /**
     * Return the boolean value this column should be set to.
     * @see #setValue(String)
     */
    public Boolean getValueBoolean() {
        return valueBoolean;
    }

    public ColumnConfig setValueBoolean(Boolean valueBoolean) {
        this.valueBoolean = valueBoolean;

        return this;
    }

    /**
     * Set the valueBoolean based on a given string.
     * If the passed value cannot be parsed as a date, it is assumed to be a function that returns a boolean.
     * If the string "null" or an empty string is passed, it will set a null value.
     * If "1" is passed, defaultValueBoolean is set to true. If 0 is passed, defaultValueBoolean is set to false
     */
    public ColumnConfig setValueBoolean(String valueBoolean) {
        valueBoolean = StringUtils.trimToNull(valueBoolean);
        if (valueBoolean == null || valueBoolean.equalsIgnoreCase("null")) {
            this.valueBoolean = null;
        } else {
            if (valueBoolean.equalsIgnoreCase("true") || valueBoolean.equals("1")) {
                this.valueBoolean = true;
            } else if (valueBoolean.equalsIgnoreCase("false") || valueBoolean.equals("0")) {
                this.valueBoolean = false;
            } else {
                this.valueComputed = new DatabaseFunction(valueBoolean);
            }

        }

        return this;
    }

    /**
     * Return the function this column should be set from.
     * @see #setValue(String)
     */

    public DatabaseFunction getValueComputed() {
        return valueComputed;
    }

    public ColumnConfig setValueComputed(DatabaseFunction valueComputed) {
        this.valueComputed = valueComputed;

        return this;
    }

    public ColumnConfig setValueSequenceNext(SequenceNextValueFunction valueSequenceNext) {
        this.valueSequenceNext = valueSequenceNext;

        return this;
    }

    public SequenceNextValueFunction getValueSequenceNext() {
        return valueSequenceNext;
    }

    public ColumnConfig setValueSequenceCurrent(SequenceCurrentValueFunction valueSequenceCurrent) {
        this.valueSequenceCurrent = valueSequenceCurrent;

        return this;
    }

    public SequenceCurrentValueFunction getValueSequenceCurrent() {
        return valueSequenceCurrent;
    }

    /**
     * Return the date value this column should be set to.
     * @see #setValue(String)
     */
    public Date getValueDate() {
        return valueDate;
    }

    public ColumnConfig setValueDate(Date valueDate) {
        this.valueDate = valueDate;

        return this;
    }

    /**
     * Set the date this column should be set to. Supports any of the date or datetime formats handled by {@link ISODateFormat}.
     * If the passed value cannot be parsed as a date, it is assumed to be a function that returns a date.
     * If the string "null" is passed, it will set a null value.
     */
    public ColumnConfig setValueDate(String valueDate) {
        if (valueDate == null || valueDate.equalsIgnoreCase("null")) {
            this.valueDate = null;
        } else {
            try {
                this.valueDate = new ISODateFormat().parse(valueDate);
            } catch (ParseException e) {
                //probably a function
                this.valueComputed = new DatabaseFunction(valueDate);
            }
        }

        return this;
    }

    /**
     * Return the file containing the data to load into a BLOB.
     * @see #setValue(String)
     */
    public String getValueBlobFile() {
        return valueBlobFile;
    }

    public ColumnConfig setValueBlobFile(String valueBlobFile) {
        this.valueBlobFile = valueBlobFile;
        return this;
    }

    /**
     * Return the file containing the data to load into a CLOB.
     * @see #setValue(String)
     */
    public String getValueClobFile() {
        return valueClobFile;
    }

    public ColumnConfig setValueClobFile(String valueClobFile) {
        this.valueClobFile = valueClobFile;
        return this;
    }

    /**
     * Return encoding of a file, referenced via {@link #valueClobFile}.
     */
    public String getEncoding() {
        return encoding;
    }
    
    public ColumnConfig setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }
    
    /**
     * Return the value from whatever setValue* function was called. Will return null if none were set.
     */
    public Object getValueObject() {
        if (getValue() != null) {
            return getValue();
        } else if (getValueBoolean() != null) {
            return getValueBoolean();
        } else if (getValueNumeric() != null) {
            return getValueNumeric();
        } else if (getValueDate() != null) {
            return getValueDate();
        } else if (getValueComputed() != null) {
            return getValueComputed();
        } else if (getValueClobFile() != null) {
            return getValueClobFile();
        } else if (getValueBlobFile() != null) {
            return getValueBlobFile();
        } else if (getValueSequenceNext() != null) {
            return getValueSequenceNext();
        } else if (getValueSequenceCurrent() != null) {
            return getValueSequenceCurrent();
        }
        return null;
    }


    /**
     * The String default value to assign to this column. If you do not want the default set by {@link #setDefaultValue(String)}
     * use a more specific function like {@link #getDefaultValueNumeric()} or the more generic {@link #getDefaultValueObject()}
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the string default value to assign to this column. If you are trying to set a default value type other than a string, use the more specific functions
     * like {@link #setDefaultValueNumeric(Number)}.
     * This method does no processing of the string. Any trimming is expected to be done beforehand. It does not convert the string "null" to null
     * so that you can set the string "null" as a value if you are feeling particularly crazy.
     */
    public ColumnConfig setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;

        return this;
    }


    /**
     * Return the numeric value this column should default to.
     * @see #setDefaultValue(String)
     */
    public Number getDefaultValueNumeric() {
        return defaultValueNumeric;
    }

    public ColumnConfig setDefaultValueNumeric(Number defaultValueNumeric) {
        this.defaultValueNumeric = defaultValueNumeric;

        return this;
    }

    /**
     * Set the number this column should use as its default. Supports integers and decimals, and strips off any wrapping parentheses.
     * If the passed value cannot be parsed as a number, it is assumed to be a function that returns a number.
     * If the value "null" is passed, it will set a null value.
     * <p></p>
     * A special case is made for "GENERATED_BY_DEFAULT" which simply sets the ColumnConfig object to autoIncrement.
     */
    public ColumnConfig setDefaultValueNumeric(String defaultValueNumeric) {
        if (defaultValueNumeric == null || defaultValueNumeric.equalsIgnoreCase("null")) {
            this.defaultValueNumeric = null;
        } else {
            if ("GENERATED_BY_DEFAULT".equals(defaultValueNumeric)) {
                setAutoIncrement(true);
            } else {
                if (defaultValueNumeric.startsWith("(")) {
                    defaultValueNumeric = defaultValueNumeric.replaceFirst("^\\(", "");
                    defaultValueNumeric = defaultValueNumeric.replaceFirst("\\)$", "");
                }
                try {
                    this.defaultValueNumeric = NumberFormat.getInstance(Locale.US).parse(defaultValueNumeric);
                } catch (ParseException e) {
                    this.defaultValueComputed = new DatabaseFunction(defaultValueNumeric);
                }
            }
        }

        return this;
    }

    /**
     * Return the date value this column should default to.
     * @see #setDefaultValue(String)
     */
    public Date getDefaultValueDate() {
        return defaultValueDate;
    }

    /**
     * Set the date this column should default to. Supports any of the date or datetime formats handled by {@link ISODateFormat}.
     * If the passed value cannot be parsed as a date, it is assumed to be a function that returns a date.
     * If the string "null" or an empty string is passed, it will set a null value.
     */
    public ColumnConfig setDefaultValueDate(String defaultValueDate) {
        defaultValueDate = StringUtils.trimToNull(defaultValueDate);
        if (defaultValueDate == null || defaultValueDate.equalsIgnoreCase("null")) {
            this.defaultValueDate = null;
        } else {
            try {
                this.defaultValueDate = new ISODateFormat().parse(defaultValueDate);
            } catch (ParseException e) {
                //probably a computed date
                this.defaultValueComputed = new DatabaseFunction(defaultValueDate);
            }
        }

        return this;
    }

    public ColumnConfig setDefaultValueDate(Date defaultValueDate) {
        this.defaultValueDate = defaultValueDate;

        return this;
    }

    /**
     * Return the boolean value this column should default to.
     * @see #setDefaultValue(String)
     */
    public Boolean getDefaultValueBoolean() {
        return defaultValueBoolean;
    }

    public ColumnConfig setDefaultValueBoolean(Boolean defaultValueBoolean) {
        this.defaultValueBoolean = defaultValueBoolean;

        return this;
    }

    /**
     * Set the defaultValueBoolean based on a given string.
     * If the passed value cannot be parsed as a date, it is assumed to be a function that returns a boolean.
     * If the string "null" or an empty string is passed, it will set a null value.
     * If "1" is passed, defaultValueBoolean is set to true. If 0 is passed, defaultValueBoolean is set to false
     */
    public ColumnConfig setDefaultValueBoolean(String defaultValueBoolean) {
        defaultValueBoolean = StringUtils.trimToNull(defaultValueBoolean);
        if (defaultValueBoolean == null || defaultValueBoolean.equalsIgnoreCase("null")) {
            this.defaultValueBoolean = null;
        } else {
            if (defaultValueBoolean.equalsIgnoreCase("true") || defaultValueBoolean.equals("1")) {
                this.defaultValueBoolean = true;
            } else if (defaultValueBoolean.equalsIgnoreCase("false") || defaultValueBoolean.equals("0")) {
                this.defaultValueBoolean = false;
            } else {
                this.defaultValueComputed = new DatabaseFunction(defaultValueBoolean);
            }

        }

        return this;
    }

    /**
     * Return the function whose value should generate this column's default.
     * @see #setDefaultValue(String)
     */
    public DatabaseFunction getDefaultValueComputed() {
        return defaultValueComputed;
    }

    public ColumnConfig setDefaultValueComputed(DatabaseFunction defaultValueComputed) {
        this.defaultValueComputed = defaultValueComputed;

        return this;
    }

    /**
     * Return the value to set this column's default to according to the setDefaultValue* function that was called.
     * If none were called, this function returns null.
     */
    public Object getDefaultValueObject() {
        if (getDefaultValue() != null) {
            return getDefaultValue();
        } else if (getDefaultValueBoolean() != null) {
            return getDefaultValueBoolean();
        } else if (getDefaultValueNumeric() != null) {
            return getDefaultValueNumeric();
        } else if (getDefaultValueDate() != null) {
            return getDefaultValueDate();
        } else if (getDefaultValueComputed() != null) {
            return getDefaultValueComputed();
        } else if (getDefaultValueSequenceNext() != null) {
            return getDefaultValueSequenceNext();
        }
        return null;
    }

    /**
     * Returns the ConstraintsConfig this ColumnConfig is using. Returns null if nho constraints have been assigned yet.
     */
    public ConstraintsConfig getConstraints() {
        return constraints;
    }

    public ColumnConfig setConstraints(ConstraintsConfig constraints) {
        this.constraints = constraints;

        return this;
    }

    /**
     * Returns true if this Column should be set to be auto increment. Returns null if auto-increment hasn't been explicitly assigned.
     */
    public Boolean isAutoIncrement() {
        return autoIncrement;
    }

    public ColumnConfig setAutoIncrement(Boolean autoIncrement) {
        this.autoIncrement = autoIncrement;

        return this;
    }

    /**
     * Return the number to start auto incrementing with.
     */
    public BigInteger getStartWith() {
        return startWith;
    }

    public ColumnConfig setStartWith(BigInteger startWith) {
        this.startWith = startWith;

        return this;
    }

    /**
     * Return the amount to auto increment by.
     */
    public BigInteger getIncrementBy() {
        return incrementBy;
    }

    public ColumnConfig setIncrementBy(BigInteger incrementBy) {
        this.incrementBy = incrementBy;

        return this;
    }

    /**
     * Returns true if any of the setDefaultValue* functions have had a non-null value set
     */
    public boolean hasDefaultValue() {
        return this.getDefaultValue() != null
                || this.getDefaultValueBoolean() != null
                || this.getDefaultValueDate() != null
                || this.getDefaultValueNumeric() != null
                || this.getDefaultValueComputed() != null
                || this.getDefaultValueSequenceNext() != null;
    }

    /**
     * Return the remarks to apply to this column.
     */
    public String getRemarks() {
        return remarks;
    }

    public ColumnConfig setRemarks(String remarks) {
        this.remarks = remarks;
        return this;
    }

    @Override
    public String getSerializedObjectName() {
        return "column";
    }

    @Override
    public Set<String> getSerializableFields() {
        return ReflectionSerializer.getInstance().getFields(this);
    }

    @Override
    public Object getSerializableFieldValue(String field) {
        return ReflectionSerializer.getInstance().getValue(this, field);
    }

    public SequenceNextValueFunction getDefaultValueSequenceNext() {
        return defaultValueSequenceNext;
    }

    public ColumnConfig setDefaultValueSequenceNext(SequenceNextValueFunction defaultValueSequenceNext) {
        this.defaultValueSequenceNext = defaultValueSequenceNext;

        return this;
    }
    
    @Override
    public SerializationType getSerializableFieldType(String field) {
        return SerializationType.NAMED_FIELD;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

}
