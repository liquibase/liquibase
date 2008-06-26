package liquibase.change;

import liquibase.database.Database;
import liquibase.database.sql.AddDefaultValueStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.sql.ComputedDateValue;
import liquibase.database.sql.ComputedNumericValue;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Table;
import liquibase.exception.UnsupportedChangeException;
import liquibase.util.ISODateFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Sets a new default value to an existing column.
 */
public class AddDefaultValueChange extends AbstractChange {

    private String schemaName;
    private String tableName;
    private String columnName;
    private String defaultValue;
    private String defaultValueNumeric;
    private String defaultValueDate;
    private Boolean defaultValueBoolean;

    public AddDefaultValueChange() {
        super("addDefaultValue", "Add Default Value");
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
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

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        Object defaultValue = null;

        if (getDefaultValue() != null) {
            defaultValue = getDefaultValue();
        } else if (getDefaultValueBoolean() != null) {
            defaultValue = Boolean.valueOf(getDefaultValueBoolean());
        } else if (getDefaultValueNumeric() != null) {
            try {
                defaultValue = NumberFormat.getInstance(Locale.US).
                	parse(getDefaultValueNumeric()); 
            } catch (ParseException e) {
            	defaultValue = new ComputedNumericValue(getDefaultValueNumeric());
            }
        } else if (getDefaultValueDate() != null) {
            try {
                defaultValue = new ISODateFormat().parse(getDefaultValueDate());
            } catch (ParseException e) {
                defaultValue = new ComputedDateValue(getDefaultValueDate());
            }
        }

        return new SqlStatement[]{
                new AddDefaultValueStatement(getSchemaName() == null ? database.getDefaultSchemaName() : getSchemaName(), getTableName(), getColumnName(), defaultValue)
        };
    }

    protected Change[] createInverses() {
        DropDefaultValueChange inverse = new DropDefaultValueChange();
        inverse.setSchemaName(getSchemaName());
        inverse.setTableName(getTableName());
        inverse.setColumnName(getColumnName());

        return new Change[]{
                inverse
        };
    }

    public String getConfirmationMessage() {
        return "Default value added to " + getTableName() + "." + getColumnName();
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement(getTagName());
        if (getSchemaName() != null) {
            node.setAttribute("schemaName", getSchemaName());
        }
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

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        Column column = new Column();

        Table table = new Table(getTableName());
        column.setTable(table);

        column.setName(columnName);

        return new HashSet<DatabaseObject>(Arrays.asList(table, column));

    }

}
