package liquibase.database.statement.generator;

import liquibase.database.statement.CreateSequenceStatement;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;
import liquibase.database.*;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.exception.JDBCException;

public class CreateSequenceGenerator implements SqlGenerator<CreateSequenceStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(CreateSequenceStatement statement, Database database) {
        return database.supportsSequences();
    }

    public GeneratorValidationErrors validate(CreateSequenceStatement createSequenceStatement, Database database) {
        return new GeneratorValidationErrors();
    }

    public Sql[] generateSql(CreateSequenceStatement statement, Database database) throws JDBCException {
        StringBuffer buffer = new StringBuffer();
        buffer.append("CREATE SEQUENCE ");
        buffer.append(database.escapeSequenceName(statement.getSchemaName(), statement.getSequenceName()));
        if (statement.getStartValue() != null) {
            if (database instanceof FirebirdDatabase) {
                throw new StatementNotSupportedOnDatabaseException("Firebird does not support creating sequences with startValue", statement, database);
            } else {
                buffer.append(" START WITH ").append(statement.getStartValue());
            }
        }
        if (statement.getIncrementBy() != null) {
            if (database instanceof FirebirdDatabase) {
                throw new StatementNotSupportedOnDatabaseException("Firebird does not support creating sequences with increments", statement, database);
            } else {
                buffer.append(" INCREMENT BY ").append(statement.getIncrementBy());
            }
        }
        if (statement.getMinValue() != null) {
            if (database instanceof FirebirdDatabase || database instanceof HsqlDatabase) {
                throw new StatementNotSupportedOnDatabaseException("Database does not support creating sequences with minValue", statement, database);
            } else {
                buffer.append(" MINVALUE ").append(statement.getMinValue());
            }
        }
        if (statement.getMaxValue() != null) {
            if (database instanceof FirebirdDatabase || database instanceof HsqlDatabase) {
                throw new StatementNotSupportedOnDatabaseException("Database does not support creating sequences with maxValue", statement, database);
            } else {
                buffer.append(" MAXVALUE ").append(statement.getMaxValue());
            }
        }

        if (statement.getOrdered() != null) {
            if (database instanceof OracleDatabase || database instanceof DB2Database || database instanceof MaxDBDatabase) {
                if (statement.getOrdered()) {
                    buffer.append(" ORDER");
                }
            } else {
                throw new StatementNotSupportedOnDatabaseException("Database does not support creating sequences with 'order'", statement, database);
            }
        }

        return new Sql[] {new UnparsedSql(buffer.toString())};
    }
}
