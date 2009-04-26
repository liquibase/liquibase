package liquibase.database.statement.generator;

import liquibase.database.statement.CopyRowsStatement;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;
import liquibase.database.Database;
import liquibase.database.SQLiteDatabase;
import liquibase.exception.JDBCException;
import liquibase.change.ColumnConfig;

public class CopyRowsGenerator implements SqlGenerator<CopyRowsStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(CopyRowsStatement statement, Database database) {
        return (database instanceof SQLiteDatabase);
    }

    public GeneratorValidationErrors validate(CopyRowsStatement copyRowsStatement, Database database) {
        return new GeneratorValidationErrors();
    }

    public Sql[] generateSql(CopyRowsStatement statement, Database database) throws JDBCException {
        StringBuffer sql = new StringBuffer();
        if (database instanceof SQLiteDatabase) {
            sql.append("INSERT INTO `").append(statement.getTargetTable()).append("` SELECT ");
            for (int i = 0; i < statement.getCopyColumns().size(); i++) {
                ColumnConfig column = statement.getCopyColumns().get(i);
                if (i > 0) {
                    sql.append(",");
                }
                sql.append("`").append(column.getName()).append("`");
            }
            sql.append(" FROM `").append(statement.getSourceTable()).append("`");
        }

        return new Sql[]{
                new UnparsedSql(sql.toString())
        };
    }
}
