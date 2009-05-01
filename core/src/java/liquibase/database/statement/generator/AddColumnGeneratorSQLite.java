package liquibase.database.statement.generator;

import liquibase.database.statement.AddDefaultValueStatement;
import liquibase.database.statement.SqlStatement;
import liquibase.database.statement.AddColumnStatement;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;
import liquibase.database.Database;
import liquibase.database.DerbyDatabase;
import liquibase.database.SQLiteDatabase;
import liquibase.database.structure.Column;
import liquibase.database.structure.Table;
import liquibase.database.structure.Index;
import liquibase.change.ColumnConfig;
import liquibase.exception.JDBCException;

import java.util.List;
import java.util.ArrayList;

public class AddColumnGeneratorSQLite extends AddColumnGenerator {
     public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DATABASE_SPECIFIC;
    }

    public boolean isValidGenerator(AddColumnStatement statement, Database database) {
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
