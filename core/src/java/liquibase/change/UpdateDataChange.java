package liquibase.change;

import liquibase.database.Database;
import liquibase.database.statement.SqlStatement;
import liquibase.database.statement.UpdateStatement;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Table;
import liquibase.exception.UnsupportedChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

public class UpdateDataChange extends AbstractChange implements ChangeWithColumns {

    private String schemaName;
    private String tableName;
    private List<ColumnConfig> columns;
    private String whereClause;

    public UpdateDataChange() {
        super("update", "Update Data");
        columns = new ArrayList<ColumnConfig>();
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<ColumnConfig> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnConfig> columns) {
        this.columns = columns;
    }

    public void addColumn(ColumnConfig column) {
        columns.add(column);
    }

    public void removeColumn(ColumnConfig column) {
        columns.remove(column);
    }

    public String getWhereClause() {
        return whereClause;
    }

    public void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }

    public void validate(Database database) throws InvalidChangeDefinitionException {
        if (StringUtils.trimToNull(tableName) == null) {
            throw new InvalidChangeDefinitionException("tableName is required", this);
        }

        for (ColumnConfig column : getColumns()) {
            if (StringUtils.trimToNull(column.getName()) == null) {
                throw new InvalidChangeDefinitionException("column name is required", this);
            }
        }
    }
    
    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {

        UpdateStatement statement = new UpdateStatement(getSchemaName() == null?database.getDefaultSchemaName():getSchemaName(), getTableName());

        for (ColumnConfig column : columns) {
            statement.addNewColumnValue(column.getName(), column.getValueObject());
        }

        statement.setWhereClause(whereClause);

        return new SqlStatement[]{
                statement
        };
    }

    public String getConfirmationMessage() {
        return "Data updated in " + getTableName();
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement("update");
        if (getSchemaName() != null) {
            node.setAttribute("schemaName", getSchemaName());
        }

        node.setAttribute("tableName", getTableName());

        for (ColumnConfig col : getColumns()) {
            Element subNode = col.createNode(currentChangeLogFileDOM);
            node.appendChild(subNode);
        }

        if (StringUtils.trimToNull(getWhereClause()) != null) {
            Element whereClause = currentChangeLogFileDOM.createElement("where");
            whereClause.appendChild(currentChangeLogFileDOM.createTextNode(getWhereClause()));
            node.appendChild(whereClause);
        }
        return node;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        Table dbObject = new Table(getTableName());

        return new HashSet<DatabaseObject>(Arrays.asList(dbObject));
    }

}
