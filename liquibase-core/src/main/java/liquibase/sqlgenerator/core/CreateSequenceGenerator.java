package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.FirebirdDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.MaxDBDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateSequenceStatement;

public class CreateSequenceGenerator extends AbstractSqlGenerator<CreateSequenceStatement> {

    @Override
    public boolean supports(CreateSequenceStatement statement, Database database) {
        return database.supportsSequences();
    }

    public ValidationErrors validate(CreateSequenceStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("sequenceName", statement.getSequenceName());

        validationErrors.checkDisallowedField("startValue", statement.getStartValue(), database, FirebirdDatabase.class);
        validationErrors.checkDisallowedField("incrementBy", statement.getIncrementBy(), database, FirebirdDatabase.class);

        validationErrors.checkDisallowedField("minValue", statement.getMinValue(), database, FirebirdDatabase.class, H2Database.class, HsqlDatabase.class);
        validationErrors.checkDisallowedField("maxValue", statement.getMaxValue(), database, FirebirdDatabase.class, H2Database.class, HsqlDatabase.class);

        validationErrors.checkDisallowedField("ordered", statement.getOrdered(), database, DB2Database.class, MaxDBDatabase.class);


        return validationErrors;
    }

    public Sql[] generateSql(CreateSequenceStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("CREATE SEQUENCE ");
        buffer.append(database.escapeSequenceName(statement.getCatalogName(), statement.getSchemaName(), statement.getSequenceName()));
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
        if (statement.getCycle() != null) {
            if (statement.getCycle()) {
                buffer.append(" CYCLE");
            }
        }

        return new Sql[]{new UnparsedSql(buffer.toString())};
    }
}
