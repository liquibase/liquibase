package liquibase.change;

import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.database.SQLiteDatabase;
import liquibase.database.SQLiteDatabase.AlterTableVisitor;
import liquibase.database.sql.*;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Index;
import liquibase.database.structure.Table;
import liquibase.exception.JDBCException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Adds a column to an existing table.
 */
public class AddColumnChange extends AbstractChange implements ChangeWithColumns {

    private String schemaName;
    private String tableName;
    private List<ColumnConfig> columns;

    public AddColumnChange() {
        super("addColumn", "Add Column");
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

    public ColumnConfig getLastColumn() {
        return (columns.size() > 0) ? columns.get(columns.size() - 1) : null;
    }

    public void addColumn(ColumnConfig column) {
        columns.add(column);
    }

    public void removeColumn(ColumnConfig column) {
        columns.remove(column);
    }

    public void validate(Database database) throws InvalidChangeDefinitionException {
        if (StringUtils.trimToNull(tableName) == null) {
            throw new InvalidChangeDefinitionException("tableName is required", this);
        }

        for (ColumnConfig column : getColumns()) {
            if (StringUtils.trimToNull(column.getName()) == null) {
                throw new InvalidChangeDefinitionException("column name is required", this);
            }
            if (StringUtils.trimToNull(column.getType()) == null) {
                throw new InvalidChangeDefinitionException("column type is required", this);
            }


        }


    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
    	
//    	if (database instanceof SQLiteDatabase) {
//    		// return special statements for SQLite databases
//    		return generateStatementsForSQLiteDatabase(database);
//        }
    	
        List<SqlStatement> sql = new ArrayList<SqlStatement>();

        String schemaName = getSchemaName() == null?database.getDefaultSchemaName():getSchemaName();
        for (ColumnConfig aColumn : columns) {
            Set<ColumnConstraint> constraints = new HashSet<ColumnConstraint>();
            if (aColumn.getConstraints() != null) {
                if (aColumn.getConstraints().isNullable() != null && !aColumn.getConstraints().isNullable()) {
                    constraints.add(new NotNullConstraint());
                }
                if (aColumn.getConstraints().isPrimaryKey() != null && aColumn.getConstraints().isPrimaryKey()) {
                    constraints.add(new PrimaryKeyConstraint(aColumn.getConstraints().getPrimaryKeyName()));
                }
            }
            if (aColumn.isAutoIncrement() != null && aColumn.isAutoIncrement()) {
                constraints.add(new AutoIncrementConstraint(aColumn.getName()));
            }

            AddColumnStatement addColumnStatement = new AddColumnStatement(schemaName,
                    getTableName(),
                    aColumn.getName(),
                    aColumn.getType(),
                    aColumn.getDefaultValueObject(),
                    constraints.toArray(new ColumnConstraint[constraints.size()]));

            sql.add(addColumnStatement);

            if (aColumn.getValueObject() != null) {
                UpdateStatement updateStatement = new UpdateStatement(schemaName, getTableName());
                updateStatement.addNewColumnValue(aColumn.getName(), aColumn.getValueObject());
                sql.add(updateStatement);
            }
        }

//        for (ColumnConfig aColumn : columns) {
//            if (aColumn.getConstraints() != null) {
//                if (aColumn.getConstraints().isPrimaryKey() != null && aColumn.getConstraints().isPrimaryKey()) {
//                    AddPrimaryKeyChange change = new AddPrimaryKeyChange();
//                    change.setSchemaName(schemaName);
//                    change.setTableName(getTableName());
//                    change.setColumnNames(aColumn.getName());
//
//                    sql.addAll(Arrays.asList(change.generateStatements(database)));
//                }
//            }
//        }

        if (database instanceof DB2Database) {
            sql.add(new ReorganizeTableStatement(schemaName, getTableName()));
        }

        return sql.toArray(new SqlStatement[sql.size()]);
    }
    
    private SqlStatement[] generateStatementsForSQLiteDatabase(Database database) 
			throws UnsupportedChangeException {

		// SQLite does not support this ALTER TABLE operation until now.
		// For more information see: http://www.sqlite.org/omitted.html.
		// This is a small work around...
    	
    	List<SqlStatement> statements = new ArrayList<SqlStatement>();
    	
    	// define alter table logic
		AlterTableVisitor rename_alter_visitor = 
		new AlterTableVisitor() {
			public ColumnConfig[] getColumnsToAdd() {
				ColumnConfig[] columnsToAdd = new ColumnConfig[columns.size()];
				for (int i=0;i<columns.size();i++) {
					columnsToAdd[i] = new ColumnConfig(columns.get(i));
				}
				return columnsToAdd;
			}
			public boolean copyThisColumn(ColumnConfig column) {
				return true;
			}
			public boolean createThisColumn(ColumnConfig column) {
				return true;
			}
			public boolean createThisIndex(Index index) {
				return true;
			}
		};
    		
    	try {
    		// alter table
			statements.addAll(SQLiteDatabase.getAlterTableStatements(
					rename_alter_visitor,
					database,getSchemaName(),getTableName()));
		} catch (JDBCException e) {
			System.err.println(e);
			e.printStackTrace();
		}
    	
    	return statements.toArray(new SqlStatement[statements.size()]);
    	
    }

    protected Change[] createInverses() {
        List<Change> inverses = new ArrayList<Change>();

        for (ColumnConfig aColumn : columns) {
            if (aColumn.hasDefaultValue()) {
                DropDefaultValueChange dropChange = new DropDefaultValueChange();
                dropChange.setTableName(getTableName());
                dropChange.setColumnName(aColumn.getName());

                inverses.add(dropChange);
            }


            DropColumnChange inverse = new DropColumnChange();
            inverse.setSchemaName(getSchemaName());
            inverse.setColumnName(aColumn.getName());
            inverse.setTableName(getTableName());
            inverses.add(inverse);
        }

        return inverses.toArray(new Change[inverses.size()]);
    }

    public String getConfirmationMessage() {
        List<String> names = new ArrayList<String>(columns.size());
        for (ColumnConfig col : columns) {
            names.add(col.getName() + "(" + col.getType() + ")");
        }

        return "Columns " + StringUtils.join(names, ",") + " added to " + tableName;
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement("addColumn");
        if (getSchemaName() != null) {
            node.setAttribute("schemaName", getSchemaName());
        }
        node.setAttribute("tableName", getTableName());

        for (ColumnConfig col : getColumns()) {
            Element subNode = col.createNode(currentChangeLogFileDOM);
            node.appendChild(subNode);
        }

        return node;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        List<DatabaseObject> result = new ArrayList<DatabaseObject>(columns.size());

        Table table = new Table(getTableName());
        result.add(table);
        for (ColumnConfig aColumn : columns) {
            Column each = new Column();
            each.setTable(table);
            each.setName(aColumn.getName());
            result.add(each);
        }

        return new HashSet<DatabaseObject>(result);
    }
}
