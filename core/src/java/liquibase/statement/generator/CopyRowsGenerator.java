package liquibase.statement.generator;

import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.database.SQLiteDatabase;
import liquibase.statement.CopyRowsStatement;
import liquibase.statement.syntax.Sql;
import liquibase.statement.syntax.UnparsedSql;

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

    public Sql[] generateSql(CopyRowsStatement statement, Database database) {
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
