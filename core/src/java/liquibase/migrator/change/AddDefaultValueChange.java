package liquibase.migrator.change;

import liquibase.database.Database;
import liquibase.database.DerbyDatabase;
import liquibase.database.MSSQLDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.database.OracleDatabase;
import liquibase.database.SybaseDatabase;
import liquibase.migrator.exception.UnsupportedChangeException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Sets a new default value to an existing column.
 */
public class AddDefaultValueChange extends AbstractChange {
    private String tableName;
    private String columnName;
    private String defaultValue;
    private String defaultValueNumeric;
    private String defaultValueDate;
    private Boolean defaultValueBoolean;

    public AddDefaultValueChange() {
        super("addDefaultValue", "Add Default Value");
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

    public String[] generateStatements(Database database) throws UnsupportedChangeException {
        
        if(database instanceof SybaseDatabase) {
            return new String[]{"ALTER TABLE " + getTableName() + " REPLACE " + getColumnName() + " DEFAULT " + getColumnValue(database),};
        } else if (database instanceof MSSQLDatabase) {
            return new String[]{"ALTER TABLE " + getTableName() + " WITH NOCHECK ADD CONSTRAINT " + getColumnName() + "DefaultValue DEFAULT " + getColumnValue(database) + " FOR " + getColumnName(),};
        } else if (database instanceof MySQLDatabase) {
            return new String[]{"ALTER TABLE " + getTableName() + " ALTER " + getColumnName() + " SET DEFAULT " + getColumnValue(database),};
        } else if (database instanceof OracleDatabase) {
            return new String[]{"ALTER TABLE " + getTableName() + " MODIFY " + getColumnName() + " DEFAULT " + getColumnValue(database),};
        } else if (database instanceof DerbyDatabase) {
            return new String[]{"ALTER TABLE " + getTableName() + " ALTER COLUMN  " + getColumnName() + " WITH DEFAULT " + getColumnValue(database),};
        }

        return new String[]{
                "ALTER TABLE " + getTableName() + " ALTER COLUMN  " + getColumnName() + " SET DEFAULT " + getColumnValue(database),
        };
    }

    protected Change[] createInverses() {
        DropDefaultValueChange inverse = new DropDefaultValueChange();
        inverse.setTableName(getTableName());
        inverse.setColumnName(getColumnName());

        return new Change[]{
                inverse
        };
    }

    public String getConfirmationMessage() {
        return "Default Value Added";
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement(getTagName());
        node.setAttribute("tableName", getTableName());
        node.setAttribute("columnName", getColumnName());
        if (getDefaultValue() != null) {
            node.setAttribute("defaultValue", getDefaultValue());
        }
        if (getDefaultValueNumeric() != null) {
            node.setAttribute("defaultValueNumeric", getDefaultValueNumeric());
        }
        if (getDefaultValueDate() != null) {
            node.setAttribute("defaultValueDate", getDefaultValueDate());
        }
        if (getDefaultValueBoolean() != null) {
            node.setAttribute("defaultValueBoolean", getDefaultValueBoolean().toString());
        }

        return node;
    }

    private String getColumnValue(Database database) {
        if (getDefaultValue() != null) {
            if ("null".equalsIgnoreCase(getDefaultValue())) {
                return "NULL";
            }
            return "'" + getDefaultValue().replaceAll("'", "''") + "'";
        } else if (getDefaultValueNumeric() != null) {
            return getDefaultValueNumeric();
        } else if (getDefaultValueBoolean() != null) {
            String returnValue;
            if (getDefaultValueBoolean()) {
                returnValue = database.getTrueBooleanValue();
            } else {
                returnValue = database.getFalseBooleanValue();
            }
            if (returnValue.matches("\\d+")) {
                return returnValue;
            } else {
                return "'"+returnValue+"'";
            }
        } else if (getDefaultValueDate() != null) {
            return database.getDateLiteral(getDefaultValueDate());
        } else {
            return "NULL";
        }
    }
}
