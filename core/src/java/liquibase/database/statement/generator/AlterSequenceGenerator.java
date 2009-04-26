package liquibase.database.statement.generator;

import liquibase.database.*;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;
import liquibase.database.statement.AlterSequenceStatement;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.exception.JDBCException;

public class AlterSequenceGenerator implements SqlGenerator<AlterSequenceStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(AlterSequenceStatement statement, Database database) {
        return database.supportsSequences();
    }

    public GeneratorValidationErrors validate(AlterSequenceStatement alterSequenceStatement, Database database) {
        return new GeneratorValidationErrors();
    }

    public Sql[] generateSql(AlterSequenceStatement statement, Database database) throws JDBCException {
        StringBuffer buffer = new StringBuffer();
        buffer.append("ALTER SEQUENCE ");
        buffer.append(database.escapeSequenceName(statement.getSchemaName(), statement.getSequenceName()));

        if (statement.getIncrementBy() != null) {
            if (database instanceof FirebirdDatabase || database instanceof HsqlDatabase) {
                throw new StatementNotSupportedOnDatabaseException("Database does not support altering sequences with increment", statement, database);
            } else {
                buffer.append(" INCREMENT BY ").append(statement.getIncrementBy());
            }
        }
        if (statement.getMinValue() != null) {
            if (database instanceof FirebirdDatabase || database instanceof HsqlDatabase) {
                buffer.append(" RESTART WITH ").append(statement.getMinValue());
            } else {
                buffer.append(" MINVALUE ").append(statement.getMinValue());
            }
        }

        if (statement.getMaxValue() != null) {
            if (database instanceof FirebirdDatabase || database instanceof HsqlDatabase) {
                throw new StatementNotSupportedOnDatabaseException("Database does not support altering sequences with maxValue", statement, database);
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

        return new Sql[]{
                new UnparsedSql(buffer.toString())
        };
    }
}
