package liquibase.change;

import liquibase.exception.DateParseException;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.AbstractLiquibaseSerializable;
import liquibase.serializer.ReflectionSerializer;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.SequenceCurrentValueFunction;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.structure.core.*;
import liquibase.util.*;
import lombok.Getter;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * The standard configuration used by Change classes to represent a column.
 * It is not required that a column-based Change uses this class, but parsers should look for it so it is a helpful
 * convenience. The definitions of "defaultValue" and "value" will vary based on the Change and may not be applicable
 * in all cases.
 */
public class ColumnConfig extends AbstractLiquibaseSerializable {
    private String name;
    private Boolean computed;
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
    private String defaultValueConstraintName;

    private ConstraintsConfig constraints;
    private Boolean autoIncrement;
    private String generationType;
    private Boolean defaultOnNull;
    private BigInteger startWith;
    private BigInteger incrementBy;
    private String remarks;
    private Boolean descending;

    /**
     * Create a ColumnConfig object based on a {@link Column} snapshot.
     * It will attempt to set as much as possible based on the information in the snapshot.
     */
    public ColumnConfig(Column columnSnapshot) {
        setName(columnSnapshot.getName());
        setComputed(BooleanUtil.isTrue(columnSnapshot.getComputed()) ? Boolean.TRUE : null);
        setDescending(BooleanUtil.isTrue(columnSnapshot.getDescending()) ? Boolean.TRUE : null);
        if (columnSnapshot.getType() != null) {
            setType(columnSnapshot.getType().toString());
        }


        if (columnSnapshot.getDefaultValue() != null) {
            Object defaultValue = columnSnapshot.getDefaultValue();
            if (defaultValue instanceof Boolean) {
                setDefaultValueBoolean((Boolean) defaultValue);
            } else if (defaultValue instanceof Number) {
                setDefaultValueNumeric(defaultValue.toString());
            } else if (defaultValue instanceof SequenceNextValueFunction) {
                setDefaultValueSequenceNext((SequenceNextValueFunction) defaultValue);
            } else if (defaultValue instanceof DatabaseFunction) {
                setDefaultValueComputed((DatabaseFunction) defaultValue);
            } else if (defaultValue instanceof Date) {
                setDefaultValueDate((Date) defaultValue);
            } else {
                setDefaultValue(defaultValue.toString());
            }
        }
        setDefaultValueConstraintName(columnSnapshot.getDefaultValueConstraintName());

        boolean nonDefaultConstraints = false;
        ConstraintsConfig constraints = new ConstraintsConfig();

        if ((columnSnapshot.isNullable() != null) && !columnSnapshot.isNullable()) {
            constraints.setNullable(columnSnapshot.isNullable());
            constraints.setValidateNullable(columnSnapshot.getValidateNullable());
            nonDefaultConstraints = true;
        }


        if ((columnSnapshot.getRelation() != null) && (columnSnapshot.getRelation() instanceof Table)) {
            Table table = (Table) columnSnapshot.getRelation();
            List<NotNullConstraint> notNullConstraints = table.getNotNullConstraints();
            if (notNullConstraints != null) {
                    for (NotNullConstraint constraint : notNullConstraints) {
                            if (constraint.getColumnName().equals(getName())) {
                                    constraints.setNullable(false);
                                    constraints.setNotNullConstraintName(constraint.getConstraintName());
                                    nonDefaultConstraints = true;
                                }
                        }
                }

            if (columnSnapshot.isAutoIncrement()) {
                setAutoIncrement(true);
                setStartWith(columnSnapshot.getAutoIncrementInformation().getStartWith());
                setIncrementBy(columnSnapshot.getAutoIncrementInformation().getIncrementBy());
            } else {
                setAutoIncrement(false);
            }

            PrimaryKey primaryKey = table.getPrimaryKey();
            if ((primaryKey != null) && primaryKey.getColumnNamesAsList().contains(columnSnapshot.getName())) {
                constraints.setPrimaryKey(true);
                constraints.setPrimaryKeyName(primaryKey.getName());
                constraints.setPrimaryKeyTablespace(primaryKey.getTablespace());
                nonDefaultConstraints = true;
            }

            List<UniqueConstraint> uniqueConstraints = table.getUniqueConstraints();
            if (uniqueConstraints != null) {
                for (UniqueConstraint constraint : uniqueConstraints) {
                    if (constraint.getColumnNames().contains(getName())) {
                        constraints.setUnique(true);
                        constraints.setUniqueConstraintName(constraint.getName());
                        nonDefaultConstraints = true;
                    }
                }
            }

            List<ForeignKey> fks = table.getOutgoingForeignKeys();
            if (fks != null) {
                for (ForeignKey fk : fks) {
                    if ((fk.getForeignKeyColumns() != null) && (fk.getForeignKeyColumns().size() == 1) && fk
                        .getForeignKeyColumns().get(0).getName().equals(getName())) {
                        constraints.setForeignKeyName(fk.getName());
                        constraints.setReferences(fk.getPrimaryKeyTable().getName() +
                            "(" +
                            fk.getPrimaryKeyColumns().get(0).getName() +
                            ")");
                        constraints.setDeleteCascade(fk.getDeleteRule() != null && fk.getDeleteRule() == ForeignKeyConstraintType.importedKeyCascade);
                        nonDefaultConstraints = true;
                    }
                }
            }

            if (nonDefaultConstraints) {
                setConstraints(constraints);
            }
        }

        setRemarks(columnSnapshot.getRemarks());
    }

    /**
     * Create am empty ColumnConfig object. Boolean and other object values will default to null.
     */
    public ColumnConfig() {
    }

    public static ColumnConfig fromName(String name) {
        name = name.trim();
        Boolean descending = null;
        if (name.matches("(?i).*\\s+DESC")) {
            name = name.replaceFirst("(?i)\\s+DESC$", "");
            descending = true;
        } else if (name.matches("(?i).*\\s+ASC")) {
            name = name.replaceFirst("(?i)\\s+ASC$", "");
            descending = false;
        }
        return new ColumnConfig()
                .setName(name)
                .setDescending(descending);
    }

    public static ColumnConfig[] arrayFromNames(String names) {
        if (names == null) {
            return null;
        }
        List<String> nameArray = StringUtil.splitAndTrim(names, ",");
        ColumnConfig[] returnArray = new ColumnConfig[nameArray.size()];
        for (int i = 0; i < nameArray.size(); i++) {
            returnArray[i] = fromName(nameArray.get(i));
        }
        return returnArray;
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

    public ColumnConfig setName(String name, boolean computed) {
        setComputed(computed);
        return setName(name);
    }

    public Boolean getComputed() {
        return computed;
    }

    public ColumnConfig setComputed(Boolean computed) {
        this.computed = computed;
        return this;
    }

    /**
     * The data type fof the column.
     * This value will pass through {@link liquibase.datatype.DataTypeFactory#fromDescription(String, liquibase.database.Database)} before being included in SQL.
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
     * If performing an data manipulation operation, the setValue* functions should be used to set what the columns
     * should be set to. If performing a data definition operation, this setValue* functions should be used to set
     * what existing rows should be set to (may be different than the default value for new rows)
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the string value this column should be set to.
     * If you are trying to set a value type other than a string, use the more specific functions like {@link #setValueNumeric(Number)}.
     * This method does no processing of the string. Any trimming is expected to be done beforehand. It does not convert the string "null" to null
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

    public ColumnConfig setValueNumeric(Number valueNumeric) {
        this.valueNumeric = valueNumeric;

        return this;
    }

    /**
     * Set the number this column should be set to. Supports integers and decimals, and strips off any wrapping
     * parentheses. If the passed value cannot be parsed as a number in US locale, it is assumed to be a function
     * that returns a number. If the value "null" is passed, it will set a null value.
     */
    public ColumnConfig setValueNumeric(String valueNumeric) {
        if ((valueNumeric == null) || "null".equalsIgnoreCase(valueNumeric)) {
            this.valueNumeric = null;
        } else {
            String saved = valueNumeric;
            if (valueNumeric.startsWith("(")) {
                valueNumeric = valueNumeric.replaceFirst("^\\(", "");
                valueNumeric = valueNumeric.replaceFirst("\\)$", "");
            }

            try {
                this.valueNumeric = ValueNumeric.of(Locale.US, valueNumeric);
            } catch (ParseException e) {
                this.valueComputed = new DatabaseFunction(saved);
            }
        }

        return this;
    }

    /**
     * Return the boolean value this column should be set to.
     * @see #setValue(String)
     */
    public Boolean getValueBoolean() {
        return valueBoolean;
    }

    /**
     * Set the valueBoolean based on a given string.
     * If the passed value cannot be parsed as a boolean, it is assumed to be a function that returns a boolean.
     * If the string "null" or an empty string is passed, it will set a null value.
     * If "1" is passed, defaultValueBoolean is set to true. If 0 is passed, defaultValueBoolean is set to false
     */
    public ColumnConfig setValueBoolean(String valueBoolean) {
        valueBoolean = StringUtil.trimToNull(valueBoolean);
        if ((valueBoolean == null) || "null".equalsIgnoreCase(valueBoolean)) {
            this.valueBoolean = null;
        } else {
            if ("true".equalsIgnoreCase(valueBoolean) || "1".equals(valueBoolean)) {
                this.valueBoolean = true;
            } else if ("false".equalsIgnoreCase(valueBoolean) || "0".equals(valueBoolean)) {
                this.valueBoolean = false;
            } else {
                this.valueComputed = new DatabaseFunction(valueBoolean);
            }

        }

        return this;
    }

    public ColumnConfig setValueBoolean(Boolean valueBoolean) {
        this.valueBoolean = valueBoolean;

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

    public SequenceNextValueFunction getValueSequenceNext() {
        return valueSequenceNext;
    }

    public ColumnConfig setValueSequenceNext(SequenceNextValueFunction valueSequenceNext) {
        this.valueSequenceNext = valueSequenceNext;

        return this;
    }

    public SequenceCurrentValueFunction getValueSequenceCurrent() {
        return valueSequenceCurrent;
    }

    public ColumnConfig setValueSequenceCurrent(SequenceCurrentValueFunction valueSequenceCurrent) {
        this.valueSequenceCurrent = valueSequenceCurrent;

        return this;
    }

    /**
     * Return the date value this column should be set to.
     * @see #setValue(String)
     */
    public Date getValueDate() {
        return valueDate;
    }

    /**
     * Set the date this column should be set to. Supports any of the date or datetime formats handled by {@link ISODateFormat}.
     * If the passed value cannot be parsed as a date, it is assumed to be a function that returns a date.
     * If the string "null" is passed, it will set a null value.
     * @param valueDate the Date Value to use (may be null or "null", or start with "now" or "today").
     * @throws DateParseException if the columnType isn't supported for "now" or "today" values.
     */
    public ColumnConfig setValueDate(String valueDate) throws DateParseException {
        if ((valueDate == null) || "null".equalsIgnoreCase(valueDate)) {
            this.valueDate = null;
        } else if (NowAndTodayUtil.isNowOrTodayFormat(valueDate)) {
            this.valueDate = NowAndTodayUtil.doNowOrToday(valueDate, this.getType());
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

    public ColumnConfig setValueDate(Date valueDate) {
        this.valueDate = valueDate;

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

    /**
     * Set the number this column should use as its default. Supports integers and decimals, and strips off any wrapping parentheses.
     * If the passed value cannot be parsed as a number, it is assumed to be a function that returns a number.
     * If the value "null" is passed, it will set a null value.
     * <p></p>
     * A special case is made for "GENERATED_BY_DEFAULT" which simply sets the ColumnConfig object to autoIncrement.
     */
    public ColumnConfig setDefaultValueNumeric(String defaultValueNumeric) {
        if ((defaultValueNumeric == null) || "null".equalsIgnoreCase(defaultValueNumeric)) {
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
                    this.defaultValueNumeric = ValueNumeric.of(Locale.US, defaultValueNumeric);
                } catch (ParseException e) {
                    this.defaultValueComputed = new DatabaseFunction(defaultValueNumeric);
                }
            }
        }

        return this;
    }

    public ColumnConfig setDefaultValueNumeric(Number defaultValueNumeric) {
        this.defaultValueNumeric = defaultValueNumeric;

        return this;
    }

    /**
     * Return the date value this column should default to.
     * @see #setDefaultValue(String)
     */
    public Date getDefaultValueDate() {
        return defaultValueDate;
    }

    public ColumnConfig setDefaultValueDate(Date defaultValueDate) {
        this.defaultValueDate = defaultValueDate;

        return this;
    }

    /**
     * Set the date this column should default to. Supports any of the date or datetime formats handled by {@link ISODateFormat}.
     * If the passed value cannot be parsed as a date, it is assumed to be a function that returns a date.
     * If the string "null" or an empty string is passed, it will set a null value.
     */
    public ColumnConfig setDefaultValueDate(String defaultValueDate) {
        defaultValueDate = StringUtil.trimToNull(defaultValueDate);
        if ((defaultValueDate == null) || "null".equalsIgnoreCase(defaultValueDate)) {
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

    /**
     * Return the boolean value this column should default to.
     * @see #setDefaultValue(String)
     */
    public Boolean getDefaultValueBoolean() {
        return defaultValueBoolean;
    }

    /**
     * Set the defaultValueBoolean based on a given string.
     * If the passed value cannot be parsed as a boolean, it is assumed to be a function that returns a boolean.
     * If the string "null" or an empty string is passed, it will set a null value.
     * If "1" is passed, defaultValueBoolean is set to true. If 0 is passed, defaultValueBoolean is set to false
     */
    public ColumnConfig setDefaultValueBoolean(String defaultValueBoolean) {
        defaultValueBoolean = StringUtil.trimToNull(defaultValueBoolean);
        if ((defaultValueBoolean == null) || "null".equalsIgnoreCase(defaultValueBoolean)) {
            this.defaultValueBoolean = null;
        } else {
            if ("true".equalsIgnoreCase(defaultValueBoolean) || "1".equals(defaultValueBoolean)) {
                this.defaultValueBoolean = true;
            } else if ("false".equalsIgnoreCase(defaultValueBoolean) || "0".equals(defaultValueBoolean)) {
                this.defaultValueBoolean = false;
            } else {
                this.defaultValueComputed = new DatabaseFunction(defaultValueBoolean);
            }

        }

        return this;
    }

    public ColumnConfig setDefaultValueBoolean(Boolean defaultValueBoolean) {
        this.defaultValueBoolean = defaultValueBoolean;

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
        return (this.getDefaultValue() != null) || (this.getDefaultValueBoolean() != null) || (this
            .getDefaultValueDate() != null) || (this.getDefaultValueNumeric() != null) || (this
            .getDefaultValueComputed() != null) || (this.getDefaultValueSequenceNext() != null);
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

    public Boolean getDescending() {
        return descending;
    }

    public ColumnConfig setDescending(Boolean descending) {
        this.descending = descending;
        return this;
    }

    public Boolean getDefaultOnNull() {
        return defaultOnNull;
    }

    public ColumnConfig setDefaultOnNull(Boolean defaultOnNull) {
        this.defaultOnNull = defaultOnNull;
        return this;
    }

    public String getGenerationType() {
        return generationType;
    }

    public ColumnConfig setGenerationType(String generationType) {
        this.generationType = generationType;
        return this;
    }

    @Override
    public String getSerializedObjectName() {
        return "column";
    }

    public SequenceNextValueFunction getDefaultValueSequenceNext() {
        return defaultValueSequenceNext;
    }

    public ColumnConfig setDefaultValueSequenceNext(SequenceNextValueFunction defaultValueSequenceNext) {
        this.defaultValueSequenceNext = defaultValueSequenceNext;

        return this;
    }

    public String getDefaultValueConstraintName() {
        return defaultValueConstraintName;
    }

    public void setDefaultValueConstraintName(String defaultValueConstraintName) {
        this.defaultValueConstraintName = defaultValueConstraintName;
    }

    @Override
    public SerializationType getSerializableFieldType(String field) {
        return SerializationType.NAMED_FIELD;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        for (ParsedNode child : parsedNode.getChildren()) {
            if (!ObjectUtil.hasProperty(this, child.getName())) {
                throw new ParsedNodeException("Unexpected node: "+child.getName());
            }
        }


        name = parsedNode.getChildValue(null, "name", String.class);
        computed = parsedNode.getChildValue(null, "computed", Boolean.class);
        type = parsedNode.getChildValue(null, "type", String.class);
        encoding = parsedNode.getChildValue(null, "encoding", String.class);
        autoIncrement = parsedNode.getChildValue(null, "autoIncrement", Boolean.class);
        startWith = parsedNode.getChildValue(null, "startWith", BigInteger.class);
        incrementBy = parsedNode.getChildValue(null, "incrementBy", BigInteger.class);
        remarks = parsedNode.getChildValue(null, "remarks", String.class);
        descending = parsedNode.getChildValue(null, "descending", Boolean.class);


        value = parsedNode.getChildValue(null, "value", String.class);
        if (value == null) {
            value = StringUtil.trimToNull((String) parsedNode.getValue());
        }

        setValueNumeric(parsedNode.getChildValue(null, "valueNumeric", String.class));

        try {
            valueDate = parsedNode.getChildValue(null, "valueDate", Date.class);
        } catch (ParsedNodeException e) {
            valueComputed = new DatabaseFunction(parsedNode.getChildValue(null, "valueDate", String.class));
        }
        valueBoolean = parsedNode.getChildValue(null, "valueBoolean", Boolean.class);
        valueBlobFile = parsedNode.getChildValue(null, "valueBlobFile", String.class);
        valueClobFile = parsedNode.getChildValue(null, "valueClobFile", String.class);
        String valueComputedString = parsedNode.getChildValue(null, "valueComputed", String.class);
        if (valueComputedString != null) {
            valueComputed = new DatabaseFunction(valueComputedString);
        }
        String valueSequenceNextString = parsedNode.getChildValue(null, "valueSequenceNext", String.class);
        if (valueSequenceNextString != null) {
            valueSequenceNext = new SequenceNextValueFunction(valueSequenceNextString);
        }
        String valueSequenceCurrentString = parsedNode.getChildValue(null, "valueSequenceCurrent", String.class);
        if (valueSequenceCurrentString != null) {
            valueSequenceCurrent = new SequenceCurrentValueFunction(valueSequenceCurrentString);
        }


        defaultValueConstraintName = parsedNode.getChildValue(null, "defaultValueConstraintName", String.class);

        defaultValue = parsedNode.getChildValue(null, "defaultValue", String.class);

        setDefaultValueNumeric(parsedNode.getChildValue(null, "defaultValueNumeric", String.class));

        try {
            defaultValueDate = parsedNode.getChildValue(null, "defaultValueDate", Date.class);
        } catch (ParsedNodeException e) {
            defaultValueComputed = new DatabaseFunction(parsedNode.getChildValue(null, "defaultValueDate", String.class));
        }
        defaultValueBoolean = parsedNode.getChildValue(null, "defaultValueBoolean", Boolean.class);
        String defaultValueComputedString = parsedNode.getChildValue(null, "defaultValueComputed", String.class);
        if (defaultValueComputedString != null) {
            defaultValueComputed = new DatabaseFunction(defaultValueComputedString);
        }
        String defaultValueSequenceNextString = parsedNode.getChildValue(null, "defaultValueSequenceNext", String.class);
        if (defaultValueSequenceNextString != null) {
            defaultValueSequenceNext = new SequenceNextValueFunction(defaultValueSequenceNextString);
        }

        defaultOnNull = parsedNode.getChildValue(null, "defaultOnNull", Boolean.class);
        generationType = parsedNode.getChildValue(null, "generationType", String.class);

        loadConstraints(parsedNode.getChild(null, "constraints"));
    }

    protected void loadConstraints(ParsedNode constraintsNode) throws ParsedNodeException {
        if (constraintsNode == null) {
            return;
        }

        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setNullable(constraintsNode.getChildValue(null, "nullable", Boolean.class));
        constraints.setNotNullConstraintName(constraintsNode.getChildValue(null, "notNullConstraintName", String.class));
        constraints.setPrimaryKey(constraintsNode.getChildValue(null, "primaryKey", Boolean.class));
        constraints.setPrimaryKeyName(constraintsNode.getChildValue(null, "primaryKeyName", String.class));
        constraints.setPrimaryKeyTablespace(constraintsNode.getChildValue(null, "primaryKeyTablespace", String.class));
        constraints.setReferences(constraintsNode.getChildValue(null, "references", String.class));
        constraints.setReferencedTableCatalogName(constraintsNode.getChildValue(null, "referencedTableCatalogName", String.class));
        constraints.setReferencedTableSchemaName(constraintsNode.getChildValue(null, "referencedTableSchemaName", String.class));
        constraints.setReferencedTableName(constraintsNode.getChildValue(null, "referencedTableName", String.class));
        constraints.setReferencedColumnNames(constraintsNode.getChildValue(null, "referencedColumnNames", String.class));
        constraints.setUnique(constraintsNode.getChildValue(null, "unique", Boolean.class));
        constraints.setUniqueConstraintName(constraintsNode.getChildValue(null, "uniqueConstraintName", String.class));
        constraints.setCheckConstraint(constraintsNode.getChildValue(null, "checkConstraint", String.class));
        constraints.setDeleteCascade(constraintsNode.getChildValue(null, "deleteCascade", Boolean.class));
        constraints.setForeignKeyName(constraintsNode.getChildValue(null, "foreignKeyName", String.class));
        constraints.setInitiallyDeferred(constraintsNode.getChildValue(null, "initiallyDeferred", Boolean.class));
        constraints.setDeferrable(constraintsNode.getChildValue(null, "deferrable", Boolean.class));
        constraints.setValidateNullable(constraintsNode.getChildValue(null, "validateNullable", Boolean.class));
        constraints.setValidateUnique(constraintsNode.getChildValue(null, "validateUnique", Boolean.class));
        constraints.setValidatePrimaryKey(constraintsNode.getChildValue(null, "validatePrimaryKey", Boolean.class));
        constraints.setValidateForeignKey(constraintsNode.getChildValue(null, "validateForeignKey", Boolean.class));
        setConstraints(constraints);
    }

    public static class ValueNumeric extends Number {
        private static final long serialVersionUID = 1381154777956917462L;

        @Getter
        private final Number delegate;
        private final String value;

        public ValueNumeric(final String value, final Number numeric) {
            this.delegate = numeric;
            this.value = value;
        }

        private static ValueNumeric of(Locale locale, String value) throws ParseException {
            final Number parsedNumber = NumberFormat.getInstance(locale)
                    .parse(value);
            return new ValueNumeric(value, parsedNumber);
        }

        @Override
        public double doubleValue() {
            return delegate.doubleValue();
        }

        @Override
        public float floatValue() {
            return delegate.floatValue();
        }

        @Override
        public int intValue() {
            return delegate.intValue();
        }

        @Override
        public long longValue() {
            return delegate.longValue();
        }

        @Override
        public String toString() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (!(obj instanceof Number)) {
                return false;
            }
            return obj.toString().equals(this.toString());
        }

        @Override
        public int hashCode() {
            return this.toString().hashCode();
        }

    }

    @Override
    public Object getSerializableFieldValue(String field) {
        Object o = ReflectionSerializer.getInstance().getValue(this, field);
        if (field.equals("valueDate") || field.equals("defaultValueDate")) {
            return new ISODateFormat().format((Date)o);
        } else {
            return o;
        }
    }
}
