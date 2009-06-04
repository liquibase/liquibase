package liquibase.sqlgenerator.core;

import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.database.SQLiteDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.CopyRowsStatement;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;

public class CopyRowsGenerator implements SqlGenerator<CopyRowsStatement> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(CopyRowsStatement statement, Database database) {
        return (database instanceof SQLiteDatabase);
    }

    public ValidationErrors validate(CopyRowsStatement copyRowsStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("targetTable", copyRowsStatement.getTargetTable());
        validationErrors.checkRequiredField("sourceTable", copyRowsStatement.getSourceTable());
        validationErrors.checkRequiredField("copyColumns", copyRowsStatement.getCopyColumns());
        return validationErrors;
    }

    public Sql[] generateSql(CopyRowsStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
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
