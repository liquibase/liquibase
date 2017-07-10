package liquibase.sqlgenerator.core;

import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CopyRowsStatement;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Table;

public class CopyRowsGenerator extends AbstractSqlGenerator<CopyRowsStatement> {

    @Override
    public boolean supports(CopyRowsStatement statement, Database database) {
        return (database instanceof SQLiteDatabase);
    }

    @Override
    public ValidationErrors validate(CopyRowsStatement copyRowsStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("targetTable", copyRowsStatement.getTargetTable());
        validationErrors.checkRequiredField("sourceTable", copyRowsStatement.getSourceTable());
        validationErrors.checkRequiredField("copyColumns", copyRowsStatement.getCopyColumns());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(CopyRowsStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuffer sql = new StringBuffer();
        
        if (statement.getCopyColumns().isEmpty()) {
            return new Sql[]{};
        }
        
        if (database instanceof SQLiteDatabase) {
            sql.append("INSERT INTO `").append(statement.getTargetTable()).append("` (");

            for (int i = 0; i < statement.getCopyColumns().size(); i++) {
                ColumnConfig column = statement.getCopyColumns().get(i);
                if (i > 0) {
                    sql.append(",");
                }
                sql.append("`").append(column.getName()).append("`");
            }

            sql.append(") SELECT ");
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
                new UnparsedSql(sql.toString(), getAffectedTable(statement))
        };
    }

    protected Relation getAffectedTable(CopyRowsStatement statement) {
        return new Table().setName(statement.getTargetTable()).setSchema(null, null);
    }
}
