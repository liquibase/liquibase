package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.DatabaseException;
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
        validationErrors.checkDisallowedField("dataType", statement.getDataType(), database, DB2Database.class, HsqlDatabase.class, OracleDatabase.class, MySQLDatabase.class, MSSQLDatabase.class);

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(CreateSequenceStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder queryStringBuilder = new StringBuilder();
        queryStringBuilder.append("CREATE SEQUENCE ");
        if (database instanceof PostgresDatabase) {
            // supported only for version >= 9.5 https://www.postgresql.org/docs/9.5/sql-createsequence.html
            try {
                if (database.getDatabaseMajorVersion() > 9
                        || (database.getDatabaseMajorVersion() == 9 && database.getDatabaseMinorVersion() >= 5)) {
                    queryStringBuilder.append(" IF NOT EXISTS ");
                }
            } catch (DatabaseException e) {
                // we can not determinate the PostgreSQL version so we do not add this statement
            }
        }
        queryStringBuilder.append(database.escapeSequenceName(statement.getCatalogName(), statement.getSchemaName(), statement.getSequenceName()));
        if (database instanceof HsqlDatabase || database instanceof Db2zDatabase) {
            queryStringBuilder.append(" AS BIGINT ");
        } else if (statement.getDataType() != null) {
            queryStringBuilder.append(" AS " + statement.getDataType());
        }
        if (!(database instanceof MariaDBDatabase) && statement.getStartValue() != null) {
            queryStringBuilder.append(" START WITH ").append(statement.getStartValue());
        }
        if (statement.getIncrementBy() != null) {
            queryStringBuilder.append(" INCREMENT BY ").append(statement.getIncrementBy());
        }
        if (statement.getMinValue() != null) {
            queryStringBuilder.append(" MINVALUE ").append(statement.getMinValue());
        }
        if (statement.getMaxValue() != null) {
            queryStringBuilder.append(" MAXVALUE ").append(statement.getMaxValue());
        }
        if (database instanceof MariaDBDatabase && statement.getStartValue() != null) {
            queryStringBuilder.append(" START WITH ").append(statement.getStartValue());
        }

        if (statement.getCacheSize() != null) {
            if (database instanceof OracleDatabase || database instanceof Db2zDatabase || database instanceof PostgresDatabase) {
                if (BigInteger.ZERO.equals(statement.getCacheSize())) {
                    if (database instanceof OracleDatabase) {
                        queryStringBuilder.append(" NOCACHE ");
                    }
                } else {
                    queryStringBuilder.append(" CACHE ").append(statement.getCacheSize());
                }
            }
        }

        if (!(database instanceof MariaDBDatabase) && statement.getOrdered() != null) {
            if (!(database instanceof SybaseASADatabase)) {
                if (statement.getOrdered()) {
                    queryStringBuilder.append(" ORDER");
                } else {
                   if (database instanceof OracleDatabase) {
                       queryStringBuilder.append(" NOORDER");
                   }
                }
            }
        }
        if (!(database instanceof MariaDBDatabase) && statement.getCycle() != null) {
            if (statement.getCycle()) {
                queryStringBuilder.append(" CYCLE");
            }
        }

        return new Sql[]{new UnparsedSql(queryStringBuilder.toString(), getAffectedSequence(statement))};
    }

    protected Sequence getAffectedSequence(CreateSequenceStatement statement) {
        return new Sequence().setName(statement.getSequenceName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }

    private boolean isH2WithMinMaxSupport(Database database) {
        return H2Database.class.isAssignableFrom(database.getClass())
                && ((H2Database) database).supportsMinMaxForSequences();
    }
}
