package liquibase.change;

import liquibase.exception.DateParseException;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.AbstractLiquibaseSerializable;
import liquibase.serializer.ReflectionSerializer;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceCurrentValueFunction;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.util.ISODateFormat;
import liquibase.util.NowAndTodayUtil;
import liquibase.util.ObjectUtil;
import liquibase.util.StringUtils;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import static liquibase.util.StringUtils.isNullOrEqualsNULL;

/** Basic class for name + value[XXX] properties */
public class Param extends AbstractLiquibaseSerializable {

    private String name;
    private String type;
    private String value;
    private Number valueNumeric;
    private Date valueDate;
    private Boolean valueBoolean;
    private SequenceNextValueFunction valueSequenceNext;
    private SequenceCurrentValueFunction valueSequenceCurrent;
    private DatabaseFunction valueComputed;
    /**
     * The name
     */
    public String getName() {
        return name;
    }

    public Param setName(String name) {
        this.name = name;
        return this;
    }
    /**
     * The data type of the column.
     * This value will pass through {@link liquibase.datatype.DataTypeFactory#fromDescription(String, liquibase.database.Database)} before being included in SQL.
     */
    public String getType() {
        return type;
    }

    public Param setType(String type) {
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
     * This method does no processing of the string. Any trimming is expected to be done beforehand. It does not conver the string "null" to null
     * so that you can set the string "null" as a value if you are feeling particularly crazy.
     */
    public Param setValue(String value) {
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

    public Param setValueNumeric(Number valueNumeric) {
        this.valueNumeric = valueNumeric;
        return this;
    }

    /**
     * Set the number this param should be set to. Supports integers and decimals, and strips off any wrapping
     * parentheses. If the passed value cannot be parsed as a number in US locale, it is assumed to be a function
     * that returns a number. If the value "null" is passed, it will set a null value.
     */
    public Param setValueNumeric(String valueNumeric) {
        valueNumeric = StringUtils.trimToNull(valueNumeric);
        if (isNullOrEqualsNULL(valueNumeric)) {
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
                setValueComputedFn(saved);
            }
        }

        return this;
    }

    /**
     * Return the boolean value this param should be set to.
     * @see #setValue(String)
     */
    public Boolean getValueBoolean() {
        return valueBoolean;
    }

    public Param setValueBoolean(Boolean valueBoolean) {
        this.valueBoolean = valueBoolean;
        return this;
    }

    /**
     * Set the valueBoolean based on a given string.
     * If the passed value cannot be parsed as a boolean, it is assumed to be a function that returns a boolean.
     * If the string "null" or an empty string is passed, it will set a null value.
     * If "1" is passed, valueBoolean is set to true. If 0 is passed, valueBoolean is set to false
     */
    public Param setValueBoolean(String valueBoolean) {
        valueBoolean = StringUtils.trimToNull(valueBoolean);
        if (isNullOrEqualsNULL(valueBoolean)) {
            this.valueBoolean = null;
        } else {
            if ("true".equalsIgnoreCase(valueBoolean) || "1".equals(valueBoolean)) {
                this.valueBoolean = true;
            } else if ("false".equalsIgnoreCase(valueBoolean) || "0".equals(valueBoolean)) {
                this.valueBoolean = false;
            } else {
                setValueComputedFn(valueBoolean);
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

    public Param setValueComputed(DatabaseFunction valueComputed) {
        this.valueComputed = valueComputed;
        return this;
    }

    public Param setValueComputedFn(String valueComputed) {
        if(null != StringUtils.trimToNull(valueComputed)) {
            this.valueComputed = new DatabaseFunction(valueComputed);
        }
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
     * @param valueDate the Date Value to use (may be null or "null", or start with "now" or "today".
     * @throws DateParseException if the columnType isn't supported for "now" or "today" values.
     */
    public Param setValueDate(String valueDate) throws DateParseException {
        if (isNullOrEqualsNULL(valueDate)) {
            this.valueDate = null;
        } else if (NowAndTodayUtil.isNowOrTodayFormat(valueDate)) {
            this.valueDate = NowAndTodayUtil.doNowOrToday(valueDate, this.getType());
        } else {
            try {
                this.valueDate = new ISODateFormat().parse(valueDate);
            } catch (ParseException e) {
                //probably a function
                setValueComputed(new DatabaseFunction(valueDate));
            }
        }

        return this;
    }

    public Param setValueDate(Date valueDate) {
        this.valueDate = valueDate;
        return this;
    }

    public SequenceNextValueFunction getValueSequenceNext() {
        return valueSequenceNext;
    }

    public Param setValueSequenceNext(SequenceNextValueFunction valueSequenceNext) {
        this.valueSequenceNext = valueSequenceNext;
        return this;
    }

    public SequenceCurrentValueFunction getValueSequenceCurrent() {
        return valueSequenceCurrent;
    }

    public Param setValueSequenceCurrent(SequenceCurrentValueFunction valueSequenceCurrent) {
        this.valueSequenceCurrent = valueSequenceCurrent;
        return this;
    }

    @Override
    public String getSerializedObjectName() {
        return "param";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        for (ParsedNode child : parsedNode.getChildren()) {
            if (!ObjectUtil.hasProperty(this, child.getName())) {
                throw new ParsedNodeException("Unexpected node: " + child.getName());
            }
        }
        name = parsedNode.getChildValue(null, "name", String.class);
        type = parsedNode.getChildValue(null, "type", String.class);
        value = parsedNode.getChildValue(null, "value", String.class);
        if (value == null) {
            value = StringUtils.trimToNull((String) parsedNode.getValue());
        }
        setValueNumeric(parsedNode.getChildValue(null, "valueNumeric", String.class));
        try {
            valueDate = parsedNode.getChildValue(null, "valueDate", Date.class);
        } catch (ParsedNodeException e) {
            setValueComputedFn(parsedNode.getChildValue(null, "valueDate", String.class));
        }
        valueBoolean = parsedNode.getChildValue(null, "valueBoolean", Boolean.class);
        setValueComputedFn(parsedNode.getChildValue(null, "valueComputed", String.class));
        String valueSequenceNextString = parsedNode.getChildValue(null, "valueSequenceNext", String.class);
        if (valueSequenceNextString != null) {
            valueSequenceNext = new SequenceNextValueFunction(valueSequenceNextString);
        }
        String valueSequenceCurrentString = parsedNode.getChildValue(null, "valueSequenceCurrent", String.class);
        if (valueSequenceCurrentString != null) {
            valueSequenceCurrent = new SequenceCurrentValueFunction(valueSequenceCurrentString);
        }

    }

    public static class ValueNumeric extends Number {
        private static final long serialVersionUID = 1381154777956917462L;

        private final Number delegate;
        private final String value;

        private ValueNumeric(final String value, final Number numeric) {
            this.delegate = numeric;
            this.value = value;
        }

        protected static ValueNumeric of(Locale locale, String value) throws ParseException {
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
            return toString().hashCode();
        }

        public Number getDelegate() {
            return delegate;
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
        } else if (getValueSequenceNext() != null) {
            return getValueSequenceNext();
        } else if (getValueSequenceCurrent() != null) {
            return getValueSequenceCurrent();
        }
        return null;
    }
}
