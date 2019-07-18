package liquibase.sqlgenerator.core;

import liquibase.change.core.StoredLogicArgumentChange;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DropProcedureStatement;
import liquibase.structure.core.Schema;
import liquibase.structure.core.StoredLogicArgument;
import liquibase.structure.core.StoredProcedure;

import java.util.List;

public class DropProcedureGenerator extends AbstractSqlGenerator<DropProcedureStatement> {
    @Override
    public ValidationErrors validate(DropProcedureStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("procedureName", statement.getProcedureName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DropProcedureStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String procedureName = database.escapeObjectName(statement.getCatalogName(), statement.getSchemaName(), statement.getProcedureName(), StoredProcedure.class);
        if (database instanceof PostgresDatabase) {
            procedureName += argumentsSignature(database, statement.getArguments());
        }
        return new Sql[] {
                new UnparsedSql("DROP PROCEDURE " + procedureName,
                        new StoredProcedure().setName(statement.getProcedureName()).setSchema(new Schema(statement.getCatalogName(), statement.getSchemaName())))
        };
    }

    /**
     * Parse list of <b>arguments</b> and construct:
     * - "(int, int)" -- list of comma-separated input argument types
     * - "()" -- empty method argument signature if mode=EMPTY entry was found
     * - "" -- empty string, if no arguments were provided and database supports {@link StoredProcedure} drop by unique name
     * - "()" -- defaults to empty method if no arguments were provided and database does not support drop by name
     * @return method argument signature in parenthesis
     */
    public static String argumentsSignature(Database database, List<StoredLogicArgumentChange> arguments) {
        if (arguments == null || arguments.isEmpty()) {
            if (supportsUniqueProcedureDrop(database)) {
                return "";
            } else {
                return "()";
            }
        }
        if (hasEmptyInputArgument(arguments)) {
            return "()";
        }
        StringBuilder argBuilder = new StringBuilder();
        for (StoredLogicArgumentChange arg : arguments) {
            if (isInputArgument(arg)) {
                if (argBuilder.length() > 0) {
                    argBuilder.append(", ");
                }
                argBuilder.append(arg.getType());
            }
        }
        return "(" + argBuilder + ")";
    }

    /**
     * @return arguments in 'mode'
     * - one of {@link StoredLogicArgument#INPUT_ARGUMENT_MODES}
     * - {@code null} -- defaults to INPUT type
     */
    private static boolean isInputArgument(StoredLogicArgumentChange arg) {
        return arg.getType() != null && (arg.getMode() == null || StoredLogicArgument.INPUT_ARGUMENT_MODES.contains(arg.getMode().toUpperCase()));
    }

    private static boolean hasEmptyInputArgument(List<StoredLogicArgumentChange> arguments) {
        if (arguments == null || arguments.isEmpty()) {
            return false;
        }
        for (StoredLogicArgumentChange arg : arguments) {
            if (StoredLogicArgument.EMPTY_ARGUMENTS_MODE.equalsIgnoreCase(arg.getMode())) {
                return true;
            }
        }
        return false;
    }

    private static boolean supportsUniqueProcedureDrop(Database database) {
        if (!(database instanceof PostgresDatabase)) {
            return true;
        }
        try {
            return (database.getDatabaseMajorVersion() > 9);
        } catch (Exception ignore) {
            return false;
        }
    }
}
