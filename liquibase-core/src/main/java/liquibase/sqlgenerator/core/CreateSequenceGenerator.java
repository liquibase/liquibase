package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
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

        validationErrors.checkDisallowedField("minValue", statement.getMinValue(), database, FirebirdDatabase.class, H2Database.class, HsqlDatabase.class);
        validationErrors.checkDisallowedField("maxValue", statement.getMaxValue(), database, FirebirdDatabase.class, H2Database.class, HsqlDatabase.class);

        validationErrors.checkDisallowedField("ordered", statement.getOrdered(), database, DB2Database.class, HsqlDatabase.class, PostgresDatabase.class);
        validationErrors.checkDisallowedField("dataType", statement.getDataType(), database, DB2Database.class, HsqlDatabase.class, OracleDatabase.class, MySQLDatabase.class, MSSQLDatabase.class);

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(CreateSequenceStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuffer queryStringBuilder = new StringBuffer();
        queryStringBuilder.append("CREATE SEQUENCE ");
        if (database instanceof PostgresDatabase) {
            queryStringBuilder.append(" IF NOT EXISTS ");
        }
        queryStringBuilder.append(database.escapeSequenceName(statement.getCatalogName(), statement.getSchemaName(), statement.getSequenceName()));
        if (database instanceof HsqlDatabase || database instanceof Db2zDatabase) {
            queryStringBuilder.append(" AS BIGINT ");
        } else if (statement.getDataType() != null) {
            queryStringBuilder.append(" AS " + statement.getDataType());
        }
        if (statement.getStartValue() != null) {
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

        if (statement.getOrdered() != null) {
            if (statement.getOrdered()) {
                queryStringBuilder.append(" ORDER");
            } else {
               if (database instanceof OracleDatabase) {
                   queryStringBuilder.append(" NOORDER");
               }
            }
        }
        if (statement.getCycle() != null) {
            if (statement.getCycle()) {
                queryStringBuilder.append(" CYCLE");
            }
        }

        return new Sql[]{new UnparsedSql(queryStringBuilder.toString(), getAffectedSequence(statement))};
    }

    protected Sequence getAffectedSequence(CreateSequenceStatement statement) {
        return new Sequence().setName(statement.getSequenceName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }
}
