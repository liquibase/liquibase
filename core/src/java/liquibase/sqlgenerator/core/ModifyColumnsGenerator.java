package liquibase.sqlgenerator.core;

import liquibase.statement.core.ModifyColumnsStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.statement.core.ReorganizeTableStatement;
import liquibase.statement.SqlStatement;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.database.Database;
import liquibase.database.structure.Index;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.change.ColumnConfig;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class ModifyColumnsGenerator implements SqlGenerator<ModifyColumnsStatement> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(ModifyColumnsStatement statement, Database database) {
        return true;
    }

    public ValidationErrors validate(ModifyColumnsStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", statement.getTableName());
        validationErrors.checkRequiredField("columns", statement.getColumns());
        return validationErrors;
    }

    public Sql[] generateSql(ModifyColumnsStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        if (database instanceof SQLiteDatabase) {
            // return special statements for SQLite databases
            return generateStatementsForSQLiteDatabase(statement, database);
        }

        List<Sql> sql = new ArrayList<Sql>();

      for (ColumnConfig aColumn : statement.getColumns()) {

          String schemaName = statement.getSchemaName() == null?database.getDefaultSchemaName():statement.getSchemaName();
          if(database instanceof SybaseASADatabase || database instanceof SybaseDatabase) {
                sql.add(new UnparsedSql("ALTER TABLE " + database.escapeTableName(schemaName, statement.getTableName()) + " MODIFY " + aColumn.getName() + " " + database.getColumnType(aColumn.getType(), false)));
        } else if (database instanceof MSSQLDatabase) {
                sql.add(new UnparsedSql("ALTER TABLE " + database.escapeTableName(schemaName, statement.getTableName()) + " ALTER COLUMN " + aColumn.getName() + " " + database.getColumnType(aColumn.getType(), false)));
        } else if (database instanceof MySQLDatabase) {
                sql.add(new UnparsedSql("ALTER TABLE " + database.escapeTableName(schemaName, statement.getTableName()) + " MODIFY COLUMN " + aColumn.getName() + " " + database.getColumnType(aColumn.getType(), false)));
        } else if (database instanceof OracleDatabase || database instanceof MaxDBDatabase || database instanceof InformixDatabase) {
                sql.add(new UnparsedSql("ALTER TABLE " + database.escapeTableName(schemaName, statement.getTableName()) + " MODIFY (" + aColumn.getName() + " " + database.getColumnType(aColumn.getType(), false) + ")"));
        } else if (database instanceof DerbyDatabase) {
                sql.add(new UnparsedSql("ALTER TABLE " + database.escapeTableName(schemaName, statement.getTableName()) + " ALTER COLUMN "+aColumn.getName()+" SET DATA TYPE " + database.getColumnType(aColumn.getType(), false)));
        } else if (database instanceof HsqlDatabase) {
                sql.add(new UnparsedSql("ALTER TABLE " + database.escapeTableName(schemaName, statement.getTableName()) + " ALTER COLUMN "+aColumn.getName()+" "+database.getColumnType(aColumn.getType(), false)));
        } else if (database instanceof CacheDatabase) {
                sql.add(new UnparsedSql("ALTER TABLE " + database.escapeTableName(schemaName, statement.getTableName()) + " ALTER COLUMN " + aColumn.getName() + " " + database.getColumnType(aColumn.getType(), false)));
        } else if (database instanceof DB2Database) {
                sql.add(new UnparsedSql("ALTER TABLE " + database.escapeTableName(schemaName, statement.getTableName()) + " ALTER COLUMN " + aColumn.getName() + " SET DATA TYPE " + database.getColumnType(aColumn.getType(), false)));
                sql.addAll(Arrays.asList(SqlGeneratorFactory.getInstance().generateSql(new ReorganizeTableStatement(schemaName, statement.getTableName()), database)));
        } else {
                sql.add(new UnparsedSql("ALTER TABLE " + database.escapeTableName(schemaName, statement.getTableName()) + " ALTER COLUMN " + aColumn.getName() + " TYPE " + database.getColumnType(aColumn.getType(), false)));
        }
      }

      return sql.toArray(new Sql[sql.size()]);

    }

    private Sql[] generateStatementsForSQLiteDatabase(final ModifyColumnsStatement statement, Database database) {

		// SQLite does not support this ALTER TABLE operation until now.
		// For more information see: http://www.sqlite.org/omitted.html.
		// This is a small work around...

    	List<Sql> statements = new ArrayList<Sql>();

    	// define alter table logic
		SQLiteDatabase.AlterTableVisitor rename_alter_visitor =
		new SQLiteDatabase.AlterTableVisitor() {
			public ColumnConfig[] getColumnsToAdd() {
				return new ColumnConfig[0];
			}
			public boolean copyThisColumn(ColumnConfig column) {
				return true;
			}
			public boolean createThisColumn(ColumnConfig column) {
				for (ColumnConfig cur_column: statement.getColumns()) {
					if (cur_column.getName().equals(column.getName())) {
						column.setType(cur_column.getType());
						break;
					}
				}
				return true;
			}
			public boolean createThisIndex(Index index) {
				return true;
			}
		};

    	try {
    		// alter table
            for (SqlStatement alterStatement : SQLiteDatabase.getAlterTableStatements(
                    rename_alter_visitor, database, statement.getSchemaName(), statement.getTableName())) {
                statements.addAll(Arrays.asList(SqlGeneratorFactory.getInstance().generateSql(alterStatement, database)));
            }

		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}

    	return statements.toArray(new Sql[statements.size()]);    	
    }

}
