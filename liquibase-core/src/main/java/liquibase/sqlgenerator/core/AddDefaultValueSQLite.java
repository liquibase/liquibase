package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.core.SQLiteDatabase;
import  liquibase.ExecutionEnvironment;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.statement.core.AddDefaultValueStatement;

public class AddDefaultValueSQLite extends AddDefaultValueGenerator {
    @Override
    public int getPriority() {
        return SqlGenerator.PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddDefaultValueStatement statement, ExecutionEnvironment env) {
        return env.getTargetDatabase() instanceof SQLiteDatabase;
    }

    @Override
    public Action[] generateActions(AddDefaultValueStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
//        // SQLite does not support this ALTER TABLE operation until now.
//		// For more information see: http://www.sqlite.org/omitted.html.
//		// This is a small work around...
//
//    	List<SqlStatement> statements = new ArrayList<SqlStatement>();
//
//		// define alter table logic
//		SQLiteDatabase.AlterTableVisitor rename_alter_visitor = new SQLiteDatabase.AlterTableVisitor() {
//			public ColumnConfig[] getColumnsToAdd() {
//				return new ColumnConfig[0];
//			}
//			public boolean copyThisColumn(ColumnConfig column) {
//				return true;
//			}
//			public boolean createThisColumn(ColumnConfig column) {
//				if (column.getName().equals(getColumnName())) {
//					try {
//						if (getDefaultValue()!=null) {
//							column.setDefaultValue(getDefaultValue());
//						}
//						if (getDefaultValueBoolean()!=null) {
//							column.setDefaultValueBoolean(getDefaultValueBoolean());
//						}
//    					if (getDefaultValueDate()!=null) {
//    						column.setDefaultValueDate(getDefaultValueDate());
//    					}
//    					if (getDefaultValueNumeric()!=null) {
//    						column.setDefaultValueNumeric(getDefaultValueNumeric());
//    					}
//					} catch (ParseException e) {
//						e.printStackTrace();
//					}
//				}
//				return true;
//			}
//			public boolean createThisIndex(Index index) {
//				return true;
//			}
//		};
//
//    	try {
//    		// alter table
//			statements.addAll(SQLiteDatabase.getAlterTableStatements(
//					rename_alter_visitor,
//					database,getSchemaName(),getTableName()));
//    	} catch (DatabaseException e) {
//			e.printStackTrace();
//		}
//
//    	return statements.toArray(new SqlStatement[statements.size()]);

        return new Action[0]; //todo
    }
}
