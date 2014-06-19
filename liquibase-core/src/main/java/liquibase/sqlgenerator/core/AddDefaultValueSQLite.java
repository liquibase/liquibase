package liquibase.sqlgenerator.core;

import liquibase.database.core.SQLiteDatabase;
import liquibase.executor.ExecutionOptions;
import liquibase.action.Sql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddDefaultValueStatement;

public class AddDefaultValueSQLite extends AddDefaultValueGenerator {
    @Override
    public int getPriority() {
        return SqlGenerator.PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddDefaultValueStatement statement, ExecutionOptions options) {
        return options.getRuntimeEnvironment().getTargetDatabase() instanceof SQLiteDatabase;
    }

    @Override
    public Sql[] generateSql(AddDefaultValueStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
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

        return new Sql[0]; //todo
    }
}
