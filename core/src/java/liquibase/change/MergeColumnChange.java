package liquibase.change;

import liquibase.database.Database;
import liquibase.database.DerbyDatabase;
import liquibase.database.sql.RawSqlStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Table;
import liquibase.exception.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Combines data from two existing columns into a new column and drops the original columns.
 */
public class MergeColumnChange extends AbstractChange {

    private String schemaName;
    private String tableName;
    private String column1Name;
    private String joinString;
    private String column2Name;
    private String finalColumnName;
    private String finalColumnType;

    public MergeColumnChange() {
        super("mergeColumns", "Merge Column");
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

    public String getColumn1Name() {
        return column1Name;
    }

    public void setColumn1Name(String column1Name) {
        this.column1Name = column1Name;
    }

    public String getJoinString() {
        return joinString;
    }

    public void setJoinString(String joinString) {
        this.joinString = joinString;
    }

    public String getColumn2Name() {
        return column2Name;
    }

    public void setColumn2Name(String column2Name) {
        this.column2Name = column2Name;
    }

    public String getFinalColumnName() {
        return finalColumnName;
    }

    public void setFinalColumnName(String finalColumnName) {
        this.finalColumnName = finalColumnName;
    }

    public String getFinalColumnType() {
        return finalColumnType;
    }

    public void setFinalColumnType(String finalColumnType) {
        this.finalColumnType = finalColumnType;
    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {

        List<SqlStatement> statements = new ArrayList<SqlStatement>();

        AddColumnChange addNewColumnChange = new AddColumnChange();
        String schemaName = getSchemaName() == null?database.getDefaultSchemaName():getSchemaName();
        addNewColumnChange.setSchemaName(schemaName);
        addNewColumnChange.setTableName(getTableName());
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setName(getFinalColumnName());
        columnConfig.setType(getFinalColumnType());
        addNewColumnChange.addColumn(columnConfig);
        statements.addAll(Arrays.asList(addNewColumnChange.generateStatements(database)));

        String updateStatement = "UPDATE " + database.escapeTableName(schemaName, getTableName()) + " SET " + getFinalColumnName() + " = " + database.getConcatSql(getColumn1Name(), "'"+getJoinString()+"'", getColumn2Name());

        statements.add(new RawSqlStatement(updateStatement));

        DropColumnChange dropColumn1Change = new DropColumnChange();
        dropColumn1Change.setSchemaName(schemaName);
        dropColumn1Change.setTableName(getTableName());
        dropColumn1Change.setColumnName(getColumn1Name());
        statements.addAll(Arrays.asList(dropColumn1Change.generateStatements(database)));

        DropColumnChange dropColumn2Change = new DropColumnChange();
        dropColumn2Change.setSchemaName(schemaName);
        dropColumn2Change.setTableName(getTableName());
        dropColumn2Change.setColumnName(getColumn2Name());
        statements.addAll(Arrays.asList(dropColumn2Change.generateStatements(database)));

        return statements.toArray(new SqlStatement[statements.size()]);

    }

    public SqlStatement[] generateStatements(@SuppressWarnings("unused") DerbyDatabase database) throws UnsupportedChangeException {
        throw new UnsupportedChangeException("Derby does not currently support merging columns");
    }

    public String getConfirmationMessage() {
        return "Columns "+getTableName()+"."+getColumn1Name()+" and "+getTableName()+"."+getColumn2Name()+" merged";
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement(getTagName());
        if (getSchemaName() != null) {
            element.setAttribute("schemaName", getSchemaName());
        }

        element.setAttribute("tableName", getTableName());
        element.setAttribute("column1Name", getColumn1Name());
        element.setAttribute("joinString", getJoinString());
        element.setAttribute("column2Name", getColumn2Name());
        element.setAttribute("finalColumnName", getFinalColumnName());
        element.setAttribute("finalColumnType", getFinalColumnType());

        return element;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        Set<DatabaseObject> returnSet = new HashSet<DatabaseObject>();
        Table table = new Table(getTableName());
        returnSet.add(table);

        Column column1 = new Column();
        column1.setTable(table);
        column1.setName(column1Name);
        returnSet.add(column1);

        Column column2 = new Column();
        column2.setTable(table);
        column2.setName(column2Name);
        returnSet.add(column2);

        return returnSet;
    }

}
