package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateProcedureStatement;
import liquibase.structure.core.Schema;
import liquibase.structure.core.StoredProcedure;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class CreateProcedureGenerator extends AbstractSqlGenerator<CreateProcedureStatement> {
    @Override
    public ValidationErrors validate(CreateProcedureStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("procedureText", statement.getProcedureText());
        if (statement.getReplaceIfExists() != null) {
            if (database instanceof MSSQLDatabase) {
                if (statement.getReplaceIfExists() && statement.getProcedureName() == null) {
                    validationErrors.addError("procedureName is required if replaceIfExists = true");
                }
            } else {
                validationErrors.checkDisallowedField("replaceIfExists", statement.getReplaceIfExists(), null);
            }

        }
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(CreateProcedureStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        List<Sql> sql = new ArrayList<Sql>();

        String procedureText = statement.getProcedureText();

        if (statement.getReplaceIfExists() != null && statement.getReplaceIfExists()) {
            sql.add(new UnparsedSql("if object_id('dbo."+statement.getProcedureName()+"', 'p') is null exec ('create procedure "+database.escapeObjectName(statement.getProcedureName(), StoredProcedure.class)+" as select 1 a')"));

            procedureText = procedureText.replaceFirst("(?i)create\\s+procedure", "ALTER PROCEDURE");
        }

        sql.add(new UnparsedSql(procedureText, statement.getEndDelimiter()));
        return sql.toArray(new Sql[sql.size()]);
    }
}