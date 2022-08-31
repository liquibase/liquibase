package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateSequenceStatement;

public class CreateSequenceGeneratorSnowflake extends CreateSequenceGenerator{

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(CreateSequenceStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(CreateSequenceStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("sequenceName", statement.getSequenceName());

        validationErrors.checkDisallowedField("minValue", statement.getMinValue(), database, SnowflakeDatabase.class);
        validationErrors.checkDisallowedField("maxValue", statement.getMaxValue(), database, SnowflakeDatabase.class);
        validationErrors.checkDisallowedField("cacheSize", statement.getCacheSize(), database, SnowflakeDatabase.class);
        validationErrors.checkDisallowedField("cycle", statement.getCycle(), database, SnowflakeDatabase.class);
        validationErrors.checkDisallowedField("datatype", statement.getDataType(), database, SnowflakeDatabase.class);
        validationErrors.checkDisallowedField("ordered", statement.getOrdered(), database, SnowflakeDatabase.class);

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(CreateSequenceStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder queryStringBuilder = new StringBuilder();
        queryStringBuilder.append("CREATE SEQUENCE ");
        queryStringBuilder.append(database.escapeSequenceName(statement.getCatalogName(), statement.getSchemaName(), statement.getSequenceName()));
        if (database instanceof SnowflakeDatabase) {
            if (statement.getStartValue() != null) {
                queryStringBuilder.append(" START WITH ").append(statement.getStartValue());
            }
            if (statement.getIncrementBy() != null) {
                queryStringBuilder.append(" INCREMENT BY ").append(statement.getIncrementBy());
            }
        }
        return new Sql[]{new UnparsedSql(queryStringBuilder.toString(), getAffectedSequence(statement))};
    }
}
