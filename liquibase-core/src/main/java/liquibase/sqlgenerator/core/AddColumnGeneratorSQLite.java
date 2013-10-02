package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddColumnStatement;

public class AddColumnGeneratorSQLite extends AddColumnGenerator {
     @Override
     public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddColumnStatement statement, Database database) {
        return database instanceof SQLiteDatabase;
    }

    @Override
    public Sql[] generateSql(final AddColumnStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
//        // SQLite does not support this ALTER TABLE operation until now.
//        // For more information see: http://www.sqlite.org/omitted.html.
//        // This is a small work around...
//
//        List<SqlStatement> statements = new ArrayList<SqlStatement>();
//
//        // define alter table logic
//        SQLiteDatabase.AlterTableVisitor rename_alter_visitor =
//        new SQLiteDatabase.AlterTableVisitor() {
//            public ColumnConfig[] getColumnsToAdd() {
//                return new ColumnConfig[] {
//                    new ColumnConfig()
//                            .setName(statement.getColumnName())
//                        .setType(statement.getDataType())
//                        .setAutoIncrement(statement.isAutoIncrement())
//                };
//            }
//            public boolean copyThisColumn(ColumnConfig column) {
//                return true;
//            }
//            public boolean createThisColumn(ColumnConfig column) {
//                return true;
//            }
//            public boolean createThisIndex(Index index) {
//                return true;
//            }
//        };
//
//        try {
//            // alter table
//            statements.addAll(SQLiteDatabase.getAlterTableStatements(rename_alter_visitor, database,statement.getSchemaName(),statement.getTableName()));
//        } catch (DatabaseException e) {
//            System.err.println(e);
//            e.printStackTrace();
//        }
//
//        return statements.toArray(new SqlStatement[statements.size()]);

        return new Sql[] {
        		new UnparsedSql("not supported. FIXME!!")
        };
    }
}
