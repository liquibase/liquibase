package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AlterSequenceStatement;
import liquibase.structure.core.Sequence;

import java.math.BigInteger;

public class AlterSequenceGenerator extends AbstractSqlGenerator<AlterSequenceStatement> {

    @Override
    public boolean supports(AlterSequenceStatement statement, Database database) {
        return database.supportsSequences();
    }

    @Override
    public ValidationErrors validate(AlterSequenceStatement alterSequenceStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkDisallowedField("incrementBy", alterSequenceStatement.getIncrementBy(), database, HsqlDatabase.class, H2Database.class);
        validationErrors.checkDisallowedField("maxValue", alterSequenceStatement.getMaxValue(), database, HsqlDatabase.class, H2Database.class);
        validationErrors.checkDisallowedField("minValue", alterSequenceStatement.getMinValue(), database, H2Database.class);
        validationErrors.checkDisallowedField("ordered", alterSequenceStatement.getOrdered(), database, HsqlDatabase.class, DB2Database.class);
        validationErrors.checkDisallowedField("dataType", alterSequenceStatement.getDataType(), database, DB2Database.class, HsqlDatabase.class, OracleDatabase.class, MySQLDatabase.class, MSSQLDatabase.class);
        validationErrors.checkRequiredField("sequenceName", alterSequenceStatement.getSequenceName());

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(AlterSequenceStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("ALTER SEQUENCE ");
        buffer.append(database.escapeSequenceName(statement.getCatalogName(), statement.getSchemaName(), statement.getSequenceName()));

        if (statement.getDataType() != null) {
            buffer.append(" AS ").append(statement.getDataType());
        }

        if (statement.getIncrementBy() != null) {
            buffer.append(" INCREMENT BY ").append(statement.getIncrementBy());
        }

        if (statement.getMinValue() != null) {
            if (database instanceof FirebirdDatabase || database instanceof HsqlDatabase || database instanceof H2Database) {
                buffer.append(" RESTART WITH ").append(statement.getMinValue());
            } else {
                buffer.append(" MINVALUE ").append(statement.getMinValue());
            }
        }

        if (statement.getMaxValue() != null) {
            buffer.append(" MAXVALUE ").append(statement.getMaxValue());
        }

        if (statement.getOrdered() != null) {
            if (statement.getOrdered()) {
                buffer.append(" ORDER");
            }
        }

        if (statement.getCacheSize() != null && (database instanceof OracleDatabase || database instanceof PostgresDatabase)) {
            if (statement.getCacheSize().equals(BigInteger.ZERO)) {
                buffer.append(" NOCACHE ");
            } else {
                buffer.append(" CACHE ").append(statement.getCacheSize());
            }
        }

        if (statement.getCycle() != null && (database instanceof OracleDatabase || database instanceof PostgresDatabase)) {
            if (statement.getCycle()) {
                buffer.append(" CYCLE ");
            } else {
                buffer.append(" NOCYCLE ");
            }
        }

        return new Sql[]{
                new UnparsedSql(buffer.toString(), getAffectedSequence(statement))
        };
    }

    protected Sequence getAffectedSequence(AlterSequenceStatement statement) {
        return new Sequence().setName(statement.getSequenceName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }
}
