package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateSequenceStatement;
import liquibase.structure.core.Sequence;

import java.math.BigInteger;

public class CreateSequenceGenerator extends AbstractSqlGenerator<CreateSequenceStatement> {

    @Override
    public boolean supports(CreateSequenceStatement statement, Database database) {
        return database.supportsSequences();
    }

    @Override
    public ValidationErrors validate(CreateSequenceStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("sequenceName", statement.getSequenceName());

        validationErrors.checkDisallowedField("startValue", statement.getStartValue(), database, FirebirdDatabase.class);
        validationErrors.checkDisallowedField("incrementBy", statement.getIncrementBy(), database, FirebirdDatabase.class);

        if (isH2WithMinMaxSupport(database)) {

            validationErrors.checkDisallowedField("minValue", statement.getMinValue(), database, FirebirdDatabase.class, HsqlDatabase.class);
            validationErrors.checkDisallowedField("maxValue", statement.getMaxValue(), database, FirebirdDatabase.class, HsqlDatabase.class);
        } else {

            validationErrors.checkDisallowedField("minValue", statement.getMinValue(), database, FirebirdDatabase.class, H2Database.class, HsqlDatabase.class);
            validationErrors.checkDisallowedField("maxValue", statement.getMaxValue(), database, FirebirdDatabase.class, H2Database.class, HsqlDatabase.class);
        }

        validationErrors.checkDisallowedField("ordered", statement.getOrdered(), database, HsqlDatabase.class, PostgresDatabase.class);


        return validationErrors;
    }

    @Override
    public Sql[] generateSql(CreateSequenceStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("CREATE SEQUENCE ");
        buffer.append(database.escapeSequenceName(statement.getCatalogName(), statement.getSchemaName(), statement.getSequenceName()));
        if (database instanceof HsqlDatabase || database instanceof Db2zDatabase) {
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

        if (statement.getCacheSize() != null) {
            if (database instanceof OracleDatabase || database instanceof Db2zDatabase) {
                if (BigInteger.ZERO.equals(statement.getCacheSize())) {
                    if (database instanceof OracleDatabase) {
                        buffer.append(" NOCACHE ");
                    }
                } else {
                    buffer.append(" CACHE ").append(statement.getCacheSize());
                }
            }
        }

        if (statement.getOrdered() != null) {
            if (!(database instanceof SybaseASADatabase)) {
                if (statement.getOrdered()) {
                    buffer.append(" ORDER");
                } else {
                   if (database instanceof OracleDatabase) {
                       buffer.append(" NOORDER");
                   }
                }
            }
        }
        if (statement.getCycle() != null) {
            if (statement.getCycle()) {
                buffer.append(" CYCLE");
            }
        }

        return new Sql[]{new UnparsedSql(buffer.toString(), getAffectedSequence(statement))};
    }

    protected Sequence getAffectedSequence(CreateSequenceStatement statement) {
        return new Sequence().setName(statement.getSequenceName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }

    private boolean isH2WithMinMaxSupport(Database database) {
        return H2Database.class.isAssignableFrom(database.getClass())
                && ((H2Database) database).supportsMinMaxForSequences();
    }
}
