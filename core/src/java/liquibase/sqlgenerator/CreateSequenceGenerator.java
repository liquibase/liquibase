package liquibase.sqlgenerator;

import liquibase.database.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.CreateSequenceStatement;

public class CreateSequenceGenerator implements SqlGenerator<CreateSequenceStatement> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(CreateSequenceStatement statement, Database database) {
        return database.supportsSequences();
    }

    public ValidationErrors validate(CreateSequenceStatement statement, Database database) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("sequenceName", statement.getSequenceName());

        if (database instanceof FirebirdDatabase) {
            validationErrors.checkDisallowedField("startValue", statement.getStartValue());
            validationErrors.checkDisallowedField("incrementBy", statement.getIncrementBy());
        }

        if (database instanceof FirebirdDatabase || database instanceof HsqlDatabase) {
            validationErrors.checkDisallowedField("minValue", statement.getMinValue());
            validationErrors.checkDisallowedField("maxValue", statement.getMaxValue());
        }

        if (database instanceof FirebirdDatabase || database instanceof HsqlDatabase) {
            validationErrors.addError("Database does not support creating sequences with maxValue");
        }

        if (statement.getOrdered() != null && !(database instanceof OracleDatabase || database instanceof DB2Database || database instanceof MaxDBDatabase)) {
            validationErrors.checkDisallowedField("ordered", statement.getOrdered());
        }


        return validationErrors;
    }

    public Sql[] generateSql(CreateSequenceStatement statement, Database database) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("CREATE SEQUENCE ");
        buffer.append(database.escapeSequenceName(statement.getSchemaName(), statement.getSequenceName()));
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

        if (statement.getOrdered() != null) {
            if (statement.getOrdered()) {
                buffer.append(" ORDER");
            }
        }

        return new Sql[]{new UnparsedSql(buffer.toString())};
    }
}
