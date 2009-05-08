package liquibase.sqlgenerator;

import liquibase.database.Database;
import liquibase.database.SQLiteDatabase;
import liquibase.sql.Sql;
import liquibase.statement.AddColumnStatement;

public class AddColumnGeneratorSQLite extends AddColumnGenerator {
     public int getPriority() {
        return PRIORITY_DATABASE;
    }

    public boolean supports(AddColumnStatement statement, Database database) {
        return database instanceof SQLiteDatabase;
    }

    public Sql[] generateSql(final AddColumnStatement statement, Database database) {
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
//                        .setType(statement.getColumnType())
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
//        } catch (JDBCException e) {
//            System.err.println(e);
//            e.printStackTrace();
//        }
//
//        return statements.toArray(new SqlStatement[statements.size()]);

        return new Sql[0]; //todo
        }
}
