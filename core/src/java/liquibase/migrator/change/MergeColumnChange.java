package liquibase.migrator.change;

import liquibase.database.Database;
import liquibase.database.DerbyDatabase;
import liquibase.migrator.exception.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Combines data from two existing columns into a new column and drops the original columns.
 */
public class MergeColumnChange extends AbstractChange {

    private String tableName;
    private String column1Name;
    private String joinString;
    private String column2Name;
    private String finalColumnName;
    private String finalColumnType;

    public MergeColumnChange() {
        super("mergeColumns", "Merge Column");
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

    public String[] generateStatements(Database database) throws UnsupportedChangeException {

        List<String> statements = new ArrayList<String>();

        AddColumnChange addNewColumnChange = new AddColumnChange();
        addNewColumnChange.setTableName(getTableName());
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setName(getFinalColumnName());
        columnConfig.setType(getFinalColumnType());
        addNewColumnChange.setColumn(columnConfig);
        statements.addAll(Arrays.asList(addNewColumnChange.generateStatements(database)));

        String updateStatement = "UPDATE " + getTableName() + " SET " + getFinalColumnName() + " = " + database.getConcatSql(getColumn1Name(), "'"+getJoinString()+"'", getColumn2Name());

        statements.add(updateStatement);

        DropColumnChange dropColumn1Change = new DropColumnChange();
        dropColumn1Change.setTableName(getTableName());
        dropColumn1Change.setColumnName(getColumn1Name());
        statements.addAll(Arrays.asList(dropColumn1Change.generateStatements(database)));

        DropColumnChange dropColumn2Change = new DropColumnChange();
        dropColumn2Change.setTableName(getTableName());
        dropColumn2Change.setColumnName(getColumn2Name());
        statements.addAll(Arrays.asList(dropColumn2Change.generateStatements(database)));

        return statements.toArray(new String[statements.size()]);

    }

    public String[] generateStatements(DerbyDatabase database) throws UnsupportedChangeException {
        throw new UnsupportedChangeException("Derby does not currently support merging columns");
    }

    public String getConfirmationMessage() {
        return "Columns Merged";
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement(getTagName());
        element.setAttribute("tableName", getTableName());
        element.setAttribute("column1Name", getColumn1Name());
        element.setAttribute("joinString", getJoinString());
        element.setAttribute("column2Name", getColumn2Name());
        element.setAttribute("finalColumnName", getFinalColumnName());
        element.setAttribute("finalColumnType", getFinalColumnType());

        return element;
    }
}
