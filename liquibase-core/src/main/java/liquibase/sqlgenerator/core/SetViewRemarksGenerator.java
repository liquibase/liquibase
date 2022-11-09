package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.SetViewRemarksStatement;
import liquibase.structure.core.Relation;
import liquibase.structure.core.View;
import liquibase.util.StringUtil;

public class SetViewRemarksGenerator extends AbstractSqlGenerator<SetViewRemarksStatement> {

    @Override
    public boolean supports(SetViewRemarksStatement statement, Database database) {
        return (database instanceof OracleDatabase) || (database instanceof PostgresDatabase);
    }

    @Override
    public ValidationErrors validate(SetViewRemarksStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("viewName", statement.getViewName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(SetViewRemarksStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String sqlPlaceholder = "COMMENT ON %s %s IS '%s'";
        String remarksEscaped = database.escapeStringForDatabase(StringUtil.trimToEmpty(statement.getRemarks()));
        String targetNameEscaped = database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getViewName());
        String targetObject;
        if (database instanceof OracleDatabase) {
            //Oracle considers views as tables for their comment syntax
            targetObject = "TABLE";
        } else {
            targetObject = "VIEW";
        }
        String sql = String.format(sqlPlaceholder, targetObject, targetNameEscaped, remarksEscaped);
        return new Sql[]{new UnparsedSql(sql, getAffectedTable(statement))};
    }

    protected Relation getAffectedTable(SetViewRemarksStatement statement) {
        return new View().setName(statement.getViewName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }
}
