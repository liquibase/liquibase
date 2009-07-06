package liquibase.change;

import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.database.structure.Column;
import liquibase.statement.ComputedDateValue;
import liquibase.statement.ComputedNumericValue;
import liquibase.statement.ColumnConstraint;
import liquibase.statement.PrimaryKeyConstraint;
import liquibase.util.ISODateFormat;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

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

    private String defaultValue;
    private Number defaultValueNumeric;
    private Date defaultValueDate;
    private Boolean defaultValueBoolean;

    private ConstraintsConfig constraints;
    private Boolean autoIncrement;
    private String remarks;
    
    
    public ColumnConfig(Column columnStructure) {
    	setName(columnStructure.getName());
		setType(columnStructure.getTypeName());
		if (columnStructure.getDefaultValue()!=null) {
			setDefaultValue(columnStructure.getDefaultValue().toString());
		}
		setAutoIncrement(columnStructure.isAutoIncrement());
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
		if (column.getConstraints()!=null) {
			ConstraintsConfig constraints = new ConstraintsConfig(); 
			constraints.setNullable(column.getConstraints().isNullable());
			constraints.setPrimaryKey(column.getConstraints().isPrimaryKey());
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
                this.valueNumeric = new ComputedNumericValue(valueNumeric);
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
        }
        try {
            this.valueDate = new ISODateFormat().parse(valueDate);
        } catch (ParseException e) {
            //probably a function
            this.valueDate = new ComputedDateValue(valueDate);
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
                this.defaultValueNumeric = NumberFormat.getInstance(Locale.US).parse(defaultValueNumeric);
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
        }
        try {
            this.defaultValueDate = new ISODateFormat().parse(defaultValueDate);
        } catch (ParseException e) {
            //probably a computed date
            this.defaultValueDate = new ComputedDateValue(defaultValueDate);
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

    public Object getDefaultValueObject() {
        if (getDefaultValue() != null) {
            return getDefaultValue();
        } else if (getDefaultValueBoolean() != null) {
            return getDefaultValueBoolean();
        } else if (getDefaultValueNumeric() != null) {
            return getDefaultValueNumeric();
        } else if (getDefaultValueDate() != null) {
            return getDefaultValueDate();
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

    public boolean isPrimaryKey() {
        return getConstraints() != null && getConstraints().isPrimaryKey();
    }

    public boolean isNullable() {
        return getConstraints() == null || getConstraints().isNullable();
    }

    public String getDefaultColumnValue(Database database) {
        if (this.getDefaultValue() != null) {
            if ("null".equalsIgnoreCase(this.getDefaultValue())) {
                return "NULL";
            }
            if (!database.shouldQuoteValue(this.getDefaultValue())) {
                return this.getDefaultValue();
            } else {
                return "'" + this.getDefaultValue().replaceAll("'", "''") + "'";
            }
        } else if (this.getDefaultValueNumeric() != null) {
            return this.getDefaultValueNumeric().toString();
        } else if (this.getDefaultValueBoolean() != null) {
            String returnValue;
            if (this.getDefaultValueBoolean()) {
                returnValue = database.getTrueBooleanValue();
            } else {
                returnValue = database.getFalseBooleanValue();
            }

            if (returnValue.matches("\\d+")) {
                return returnValue;
            } else {
                // removing the ' from the getTrueBooleanValue
                // of InformixDatabase makes troubles elsewhere
                // e.g. when creating the databasechangeloglock
                // table.
            	// getTrueBooleanValue of Informix is "'t'",
            	// no need to add another ''
            	if (database instanceof InformixDatabase) {
            		return returnValue;
            	}
                return "'" + returnValue + "'";
            }
        } else if (this.getDefaultValueDate() != null) {
            Date defaultDateValue = this.getDefaultValueDate();
            return database.getDateLiteral(defaultDateValue);
        } else {
            return "NULL";
        }
    }

    public boolean hasDefaultValue() {
        return this.getDefaultValue() != null
                || this.getDefaultValueBoolean() != null
                || this.getDefaultValueDate() != null
                || this.getDefaultValueNumeric() != null;
    }

    public String getRemarks() {
        return remarks;
    }

    public ColumnConfig setRemarks(String remarks) {
        this.remarks = remarks;
        return this;
    }
}
