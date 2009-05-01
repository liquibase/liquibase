package liquibase.database.statement.generator;

import liquibase.database.statement.AddDefaultValueStatement;
import liquibase.database.statement.SqlStatement;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.Database;
import liquibase.database.SQLiteDatabase;
import liquibase.database.structure.Index;
import liquibase.change.ColumnConfig;
import liquibase.exception.JDBCException;

import java.util.List;
import java.util.ArrayList;
import java.text.ParseException;

public class AddDefaultValueSQLite extends AddDefaultValueGenerator {
    @Override
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DATABASE_SPECIFIC;
    }

    @Override
    public boolean isValidGenerator(AddDefaultValueStatement statement, Database database) {
        return database instanceof SQLiteDatabase;
    }

    @Override
    public Sql[] generateSql(AddDefaultValueStatement statement, Database database) {
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
//    	} catch (JDBCException e) {
//			e.printStackTrace();
//		}
//
//    	return statements.toArray(new SqlStatement[statements.size()]);

        return new Sql[0]; //todo
    }
}
