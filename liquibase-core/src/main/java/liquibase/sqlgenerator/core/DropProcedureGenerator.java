package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DropProcedureStatement;
import liquibase.structure.core.Schema;
import liquibase.structure.core.StoredProcedure;
import liquibase.util.StringUtils;

public class DropProcedureGenerator extends AbstractSqlGenerator<DropProcedureStatement> {
    @Override
    public ValidationErrors validate(DropProcedureStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("procedureName", statement.getProcedureName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DropProcedureStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String fullProcedurename = database.escapeObjectName(statement.getCatalogName(), statement.getSchemaName(), statement.getProcedureName(), StoredProcedure.class);
        StoredProcedure droppedProcedure = new StoredProcedure().setName(statement.getProcedureName()).setSchema(new Schema(statement.getCatalogName(), statement.getSchemaName()));
        if (database instanceof PostgresDatabase) {
            String procedureDropName = statement.getDropName();
            String procedureArgs = StringUtils.trimToEmpty(statement.getProcedureArguments());
            String schemaName = statement.getSchemaName() == null ? database.getDefaultSchemaName() : statement.getSchemaName();
            String procedureNameWithSchema = database.escapeObjectName(schemaName, Schema.class) + "." + statement.getProcedureName();

            fullProcedurename = procedureDropName == null
                    ? String.format("%s(%s)", procedureNameWithSchema, procedureArgs)
                    : String.format("%s.%s", schemaName,  procedureDropName);
            droppedProcedure.setName(String.format("%s(%s)", statement.getProcedureName(), procedureArgs));
            droppedProcedure.setProcedureName(statement.getProcedureName());
            droppedProcedure.setDropName(procedureDropName);
            droppedProcedure.setArguments(procedureArgs);
        }
        return new Sql[] {
                new UnparsedSql("DROP PROCEDURE " + fullProcedurename, droppedProcedure)
        };
    }
}
