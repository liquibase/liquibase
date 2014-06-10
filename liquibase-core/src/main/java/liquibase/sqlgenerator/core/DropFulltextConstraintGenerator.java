package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DropFulltextConstraintStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.structure.core.FulltextConstraint;
import liquibase.util.StringUtils;

public class DropFulltextConstraintGenerator extends AbstractSqlGenerator<DropFulltextConstraintStatement> {

    @Override
    public boolean supports(DropFulltextConstraintStatement statement, Database database) {
        return !(database instanceof SQLiteDatabase);
    }

    @Override
    public ValidationErrors validate(DropFulltextConstraintStatement dropFulltextConstraintStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", dropFulltextConstraintStatement.getTableName());
        validationErrors.checkRequiredField("constraintName", dropFulltextConstraintStatement.getConstraintName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DropFulltextConstraintStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String sql="";
        if (database instanceof MySQLDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " DROP KEY " + database.escapeConstraintName(statement.getConstraintName());
        }

        return new Sql[] {
                new UnparsedSql(sql, getAffectedFulltextConstraint(statement))
        };
    }

    protected FulltextConstraint getAffectedFulltextConstraint(DropFulltextConstraintStatement statement) {
        FulltextConstraint constraint = new FulltextConstraint().setName(statement.getConstraintName()).setTable((Table) new Table().setName(statement.getTableName()).setSchema(statement.getCatalogName(), statement.getSchemaName()));
        if (statement.getFulltextColumns() != null) {
            int i = 0;
            for (String column : StringUtils.splitAndTrim(statement.getFulltextColumns(), ",")) {
                constraint.addColumn(i++, column);
            }
        }
        return constraint;
    }
}
