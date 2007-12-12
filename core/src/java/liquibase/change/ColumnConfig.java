package liquibase.change;

import liquibase.database.Database;
import liquibase.database.sql.ComputedNumericValue;
import liquibase.util.ISODateFormat;
import liquibase.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }


    public void setValue(String value) {
        // Since we have two rules for the value it can either be specifed as an attribute
        // or as the tag body in case of long values then the check is necessary so that it
        // should not override the value specifed as an attribute.
        if (StringUtils.trimToNull(value) != null) {
            this.value = value;
        }
    }

    public Number getValueNumeric() {
        return valueNumeric;
    }


    public void setValueNumeric(String valueNumeric) throws ParseException {
        if (valueNumeric == null) {
            this.valueNumeric = null;
        } else {
            if (valueNumeric.matches("\\d+\\.?\\d*")) {
                this.valueNumeric = NumberFormat.getInstance().parse(valueNumeric);
            } else {
                this.valueNumeric = new ComputedNumericValue(valueNumeric);
            }
        }
    }

    public void setValueNumeric(Number valueNumeric) {
        this.valueNumeric = valueNumeric;
    }

    public Boolean getValueBoolean() {
        return valueBoolean;
    }

    public void setValueBoolean(Boolean valueBoolean) {
        this.valueBoolean = valueBoolean;
    }

    public Date getValueDate() {
        return valueDate;
    }

    public void setValueDate(Date valueDate) {
        this.valueDate = valueDate;
    }

    public void setValueDate(String valueDate) throws ParseException {
        this.valueDate = new ISODateFormat().parse(valueDate);
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

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }


    public Number getDefaultValueNumeric() {
        return defaultValueNumeric;
    }

    public void setDefaultValueNumeric(Number defaultValueNumeric) {
        this.defaultValueNumeric = defaultValueNumeric;
    }

    public void setDefaultValueNumeric(String defaultValueNumeric) throws ParseException {
        if (defaultValueNumeric == null) {
            this.defaultValueNumeric = null;
        } else {
            if ("GENERATED_BY_DEFAULT".equals(defaultValueNumeric)) {
                setAutoIncrement(true);
            } else {
                this.defaultValueNumeric = NumberFormat.getInstance().parse(defaultValueNumeric);
            }
        }
    }

    public Date getDefaultValueDate() {
        return defaultValueDate;
    }

    public void setDefaultValueDate(String defaultValueDate) throws ParseException {
        this.defaultValueDate = new ISODateFormat().parse(defaultValueDate);
    }

    public void setDefaultValueDate(Date defaultValueDate) {
        this.defaultValueDate = defaultValueDate;
    }

    public Boolean getDefaultValueBoolean() {
        return defaultValueBoolean;
    }

    public void setDefaultValueBoolean(Boolean defaultValueBoolean) {
        this.defaultValueBoolean = defaultValueBoolean;
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

    public void setConstraints(ConstraintsConfig constraints) {
        this.constraints = constraints;
    }

    public Boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(Boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public Element createNode(Document document) {
        Element element = document.createElement("column");
        element.setAttribute("name", getName());
        if (getType() != null) {
            element.setAttribute("type", getType());
        }

        if (getDefaultValue() != null) {
            element.setAttribute("defaultValue", getDefaultValue());
        }
        if (getDefaultValueNumeric() != null) {
            element.setAttribute("defaultValueNumeric", getDefaultValueNumeric().toString());
        }
        if (getDefaultValueDate() != null) {
            element.setAttribute("defaultValueDate", new ISODateFormat().format(getDefaultValueDate()));
        }
        if (getDefaultValueBoolean() != null) {
            element.setAttribute("defaultValueBoolean", getDefaultValueBoolean().toString());
        }

        if (getValue() != null) {
            element.setAttribute("value", getValue());
        }
        if (getValueNumeric() != null) {
            element.setAttribute("valueNumeric", getValueNumeric().toString());
        }
        if (getValueBoolean() != null) {
            element.setAttribute("valueBoolean", getValueBoolean().toString());
        }
        if (getValueDate() != null) {
            element.setAttribute("valueDate", new ISODateFormat().format(getValueDate()));
        }

        if (isAutoIncrement() != null && isAutoIncrement()) {
            element.setAttribute("autoIncrement", "true");
        }

        ConstraintsConfig constraints = getConstraints();
        if (constraints != null) {
            Element constraintsElement = document.createElement("constraints");
            if (constraints.getCheck() != null) {
                constraintsElement.setAttribute("check", constraints.getCheck());
            }
            if (constraints.getForeignKeyName() != null) {
                constraintsElement.setAttribute("foreignKeyName", constraints.getForeignKeyName());
            }
            if (constraints.getReferences() != null) {
                constraintsElement.setAttribute("references", constraints.getReferences());
            }
            if (constraints.isDeferrable() != null) {
                constraintsElement.setAttribute("deferrable", constraints.isDeferrable().toString());
            }
            if (constraints.isDeleteCascade() != null) {
                constraintsElement.setAttribute("deleteCascade", constraints.isDeleteCascade().toString());
            }
            if (constraints.isInitiallyDeferred() != null) {
                constraintsElement.setAttribute("initiallyDeferred", constraints.isInitiallyDeferred().toString());
            }
            if (constraints.isNullable() != null) {
                constraintsElement.setAttribute("nullable", constraints.isNullable().toString());
            }
            if (constraints.isPrimaryKey() != null) {
                constraintsElement.setAttribute("primaryKey", constraints.isPrimaryKey().toString());
            }
            if (constraints.isUnique() != null) {
                constraintsElement.setAttribute("unique", constraints.isUnique().toString());
            }
            element.appendChild(constraintsElement);
        }

        return element;
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

}
