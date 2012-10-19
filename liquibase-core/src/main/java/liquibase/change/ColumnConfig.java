package liquibase.change;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import liquibase.database.structure.Column;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceFunction;
import liquibase.util.ISODateFormat;

/**
 * This class is the representation of the column tag in the XMl file
 * It has a reference to the Constraints object for getting information
 * about the columns constraints.
 */
public class ColumnConfig {
    private String name;
    private String type;
    private String value;
    private Number valueNumeric;
    private Date valueDate;
    private Boolean valueBoolean;
    private String valueBlob;
    private String valueClob;
    private DatabaseFunction valueComputed;
    private SequenceFunction valueSequenceNext;

    private String defaultValue;
    private Number defaultValueNumeric;
    private Date defaultValueDate;
    private Boolean defaultValueBoolean;
    private DatabaseFunction defaultValueComputed;

    private ConstraintsConfig constraints;
    private Boolean autoIncrement;
    private BigInteger startWith;
    private BigInteger incrementBy;
    private String remarks;
    
    
    public ColumnConfig(Column columnStructure) {
    	setName(columnStructure.getName());
		setType(columnStructure.getType().toString());
		if (columnStructure.getDefaultValue()!=null) {
			setDefaultValue(columnStructure.getDefaultValue().toString());
		}
        if (columnStructure.getType().isAutoIncrement()) {
		    setAutoIncrement(true);
            setStartWith(columnStructure.getType().getAutoIncrementInformation().getStartWith());
            setIncrementBy(columnStructure.getType().getAutoIncrementInformation().getIncrementBy());
        }
        ConstraintsConfig constraints = new ConstraintsConfig();
		constraints.setNullable(columnStructure.isNullable());
		constraints.setPrimaryKey(columnStructure.isPrimaryKey());
		constraints.setUnique(columnStructure.isUnique());
		setConstraints(constraints);
    }
    
    public ColumnConfig(ColumnConfig column) {
    	setName(column.getName());
		setType(column.getType());
		setDefaultValue(column.getDefaultValue());
		setAutoIncrement(column.isAutoIncrement());
		setStartWith(column.getStartWith());
		setIncrementBy(column.getIncrementBy());
		if (column.getConstraints()!=null) {
			ConstraintsConfig constraints = new ConstraintsConfig(); 
			constraints.setNullable(column.getConstraints().isNullable());
			constraints.setPrimaryKey(column.getConstraints().isPrimaryKey());
			constraints.setPrimaryKeyTablespace(column.getConstraints().getPrimaryKeyTablespace());
			constraints.setUnique(column.getConstraints().isUnique());
		}
		setConstraints(constraints);
    }
    
    public ColumnConfig() {
    	// do nothing
    }
    

    public String getName() {
        return name;
    }

    public ColumnConfig setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public ColumnConfig setType(String type) {
        this.type = type;
        return this;
    }

    public String getValue() {
        return value;
    }


    public void setValue(String value) {
        // Since we have two rules for the value it can either be specifed as an attribute
        // or as the tag body in case of long values then the check is necessary so that it
        // should not override the value specifed as an attribute.
//        if (StringUtils.trimToNull(value) != null) {
//            this.value = value;
//        }
    	// TODO find where this is being called with the tag body 
    	// and fix the code there.  this logic does not belong here
    	// because it prevents a column from being the empty string
    	this.value = value;
    }

    public Number getValueNumeric() {
        return valueNumeric;
    }


    public ColumnConfig setValueNumeric(String valueNumeric) {
        if (valueNumeric == null || valueNumeric.equalsIgnoreCase("null")) {
            this.valueNumeric = null;
        } else {
            valueNumeric = valueNumeric.replaceFirst("^\\(", "");
            valueNumeric = valueNumeric.replaceFirst("\\)$", "");
            
            if (valueNumeric.matches("\\d+\\.?\\d*")) {
                try {
                    this.valueNumeric = NumberFormat.getInstance(Locale.US).
                    	parse(valueNumeric);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            } else {
                this.valueComputed = new DatabaseFunction(valueNumeric);
            }
        }

        return this;
    }

    public ColumnConfig setValueNumeric(Number valueNumeric) {
        this.valueNumeric = valueNumeric;

        return this;
    }

    public Boolean getValueBoolean() {
        return valueBoolean;
    }

    public ColumnConfig setValueBoolean(Boolean valueBoolean) {
        this.valueBoolean = valueBoolean;

        return this;
    }

    public DatabaseFunction getValueComputed() {
        return valueComputed;
    }

    public ColumnConfig setValueComputed(DatabaseFunction valueComputed) {
        this.valueComputed = valueComputed;

        return this;
    }

    public SequenceFunction getValueSequenceNext() {
        return valueSequenceNext;
    }

    public void setValueSequenceNext(final SequenceFunction valueSequenceNext) {
        this.valueSequenceNext = valueSequenceNext;
    }

    public Date getValueDate() {
        return valueDate;
    }

    public ColumnConfig setValueDate(Date valueDate) {
        this.valueDate = valueDate;

        return this;
    }

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
        }
        return null;
    }


    public String getDefaultValue() {
        return defaultValue;
    }

    public ColumnConfig setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;

        return this;
    }


    public Number getDefaultValueNumeric() {
        return defaultValueNumeric;
    }

    public ColumnConfig setDefaultValueNumeric(Number defaultValueNumeric) {
        this.defaultValueNumeric = defaultValueNumeric;

        return this;
    }

    public ColumnConfig setDefaultValueNumeric(String defaultValueNumeric) throws ParseException {
        if (defaultValueNumeric == null || defaultValueNumeric.equalsIgnoreCase("null")) {
            this.defaultValueNumeric = null;
        } else {
            if ("GENERATED_BY_DEFAULT".equals(defaultValueNumeric)) {
                setAutoIncrement(true);
            } else {
                defaultValueNumeric = defaultValueNumeric.replaceFirst("^\\(", "");
                defaultValueNumeric = defaultValueNumeric.replaceFirst("\\)$", "");
                try {
                    this.defaultValueNumeric = NumberFormat.getInstance(Locale.US).parse(defaultValueNumeric);
                } catch (ParseException e) {
                    this.defaultValueComputed  = new DatabaseFunction(defaultValueNumeric);
                }
            }
        }

        return this;
    }

    public Date getDefaultValueDate() {
        return defaultValueDate;
    }

    public ColumnConfig setDefaultValueDate(String defaultValueDate) {
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

    public Boolean getDefaultValueBoolean() {
        return defaultValueBoolean;
    }

    public ColumnConfig setDefaultValueBoolean(Boolean defaultValueBoolean) {
        this.defaultValueBoolean = defaultValueBoolean;

        return this;
    }

    public DatabaseFunction getDefaultValueComputed() {
        return defaultValueComputed;
    }

    public ColumnConfig setDefaultValueComputed(DatabaseFunction defaultValueComputed) {
        this.defaultValueComputed = defaultValueComputed;

        return this;
    }

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
        }
        return null;
    }

    public ConstraintsConfig getConstraints() {
        return constraints;
    }

    public ColumnConfig setConstraints(ConstraintsConfig constraints) {
        this.constraints = constraints;

        return this;
    }

    public Boolean isAutoIncrement() {
        return autoIncrement;
    }

    public ColumnConfig setAutoIncrement(Boolean autoIncrement) {
        this.autoIncrement = autoIncrement;

        return this;
    }

    public BigInteger getStartWith() {
    	return startWith;
    }
    
    public ColumnConfig setStartWith(BigInteger startWith) {
    	this.startWith = startWith;
    	
    	return this;
    }
    
    public BigInteger getIncrementBy() {
    	return incrementBy;
    }
    
    public ColumnConfig setIncrementBy(BigInteger incrementBy) {
    	this.incrementBy = incrementBy;
    	
    	return this;
    }
    
    public boolean isPrimaryKey() {
        return getConstraints() != null && getConstraints().isPrimaryKey() != null && getConstraints().isPrimaryKey();
    }

    public boolean isNullable() {
        return getConstraints() == null || getConstraints().isNullable();
    }

    public boolean hasDefaultValue() {
        return this.getDefaultValue() != null
                || this.getDefaultValueBoolean() != null
                || this.getDefaultValueDate() != null
                || this.getDefaultValueNumeric() != null
                || this.getDefaultValueComputed() != null;
    }

    public String getRemarks() {
        return remarks;
    }

    public ColumnConfig setRemarks(String remarks) {
        this.remarks = remarks;
        return this;
    }

    public String getValueBlob() {
        return valueBlob;
    }

    public void setValueBlob(String valueBlob) {
        this.valueBlob = valueBlob;
    }

    public String getValueClob() {
        return valueClob;
    }

    public void setValueClob(String valueClob) {
        this.valueClob = valueClob;
    }
}
