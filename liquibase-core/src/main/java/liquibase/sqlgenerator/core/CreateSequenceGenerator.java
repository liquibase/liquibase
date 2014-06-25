package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.CreateSequenceStatement;

public class CreateSequenceGenerator extends AbstractSqlGenerator<CreateSequenceStatement> {

    @Override
    public boolean supports(CreateSequenceStatement statement, ExecutionEnvironment env) {
        return env.getTargetDatabase().supportsSequences();
    }

    @Override
    public ValidationErrors validate(CreateSequenceStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();

        Database database = env.getTargetDatabase();
        validationErrors.checkRequiredField("sequenceName", statement.getSequenceName());

        validationErrors.checkDisallowedField("startValue", statement.getStartValue(), database, FirebirdDatabase.class);
        validationErrors.checkDisallowedField("incrementBy", statement.getIncrementBy(), database, FirebirdDatabase.class);

        validationErrors.checkDisallowedField("minValue", statement.getMinValue(), database, FirebirdDatabase.class, H2Database.class, HsqlDatabase.class);
        validationErrors.checkDisallowedField("maxValue", statement.getMaxValue(), database, FirebirdDatabase.class, H2Database.class, HsqlDatabase.class);

        validationErrors.checkDisallowedField("ordered", statement.getOrdered(), database, DB2Database.class, HsqlDatabase.class);


        return validationErrors;
    }

    @Override
    public Action[] generateActions(CreateSequenceStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        Database database = env.getTargetDatabase();

        StringBuffer buffer = new StringBuffer();
        buffer.append("CREATE SEQUENCE ");
        buffer.append(database.escapeSequenceName(statement.getCatalogName(), statement.getSchemaName(), statement.getSequenceName()));
        if (database instanceof HsqlDatabase) {
            buffer.append(" AS BIGINT ");
        }
        if (statement.getStartValue() != null) {
            buffer.append(" START WITH ").append(statement.getStartValue());
        }
        if (statement.getIncrementBy() != null) {
            buffer.append(" INCREMENT BY ").append(statement.getIncrementBy());
        }
        if (statement.getMinValue() != null) {
            buffer.append(" MINVALUE ").append(statement.getMinValue());
        }
        if (statement.getMaxValue() != null) {
            buffer.append(" MAXVALUE ").append(statement.getMaxValue());
        }

        if (statement.getCacheSize() != null && database instanceof OracleDatabase) {
            buffer.append(" CACHE ").append(statement.getCacheSize());
        }

        if (statement.getOrdered() != null) {
            if (statement.getOrdered()) {
                buffer.append(" ORDER");
            } else {
               if (database instanceof OracleDatabase) {
                   buffer.append(" NOORDER");
               }
            }
        }
        if (statement.getCycle() != null) {
            if (statement.getCycle()) {
                buffer.append(" CYCLE");
            }
        }

        return new Action[]{new UnparsedSql(buffer.toString())};
    }
}
