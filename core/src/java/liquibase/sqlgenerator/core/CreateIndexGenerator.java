package liquibase.sqlgenerator.core;

import liquibase.database.*;
import liquibase.database.core.DB2Database;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.core.CreateIndexStatement;
import liquibase.util.StringUtils;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;

import java.util.Arrays;
import java.util.Iterator;

public class CreateIndexGenerator implements SqlGenerator<CreateIndexStatement> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(CreateIndexStatement statement, Database database) {
        return true;
    }

    public ValidationErrors validate(CreateIndexStatement createIndexStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", createIndexStatement.getTableName());
        validationErrors.checkRequiredField("columns", createIndexStatement.getColumns());
        return validationErrors;
    }

    public Sql[] generateSql(CreateIndexStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuffer buffer = new StringBuffer();

        buffer.append("CREATE ");
        if (statement.isUnique() != null && statement.isUnique()) {
            buffer.append("UNIQUE ");
        }
        buffer.append("INDEX ");

        buffer.append(database.escapeIndexName(null, statement.getIndexName())).append(" ON ");
        buffer.append(database.escapeTableName(statement.getTableSchemaName(), statement.getTableName())).append("(");
        Iterator<String> iterator = Arrays.asList(statement.getColumns()).iterator();
        while (iterator.hasNext()) {
            String column = iterator.next();
            buffer.append(database.escapeColumnName(statement.getTableSchemaName(), statement.getTableName(), column));
            if (iterator.hasNext()) {
                buffer.append(", ");
            }
        }
        buffer.append(")");

        if (StringUtils.trimToNull(statement.getTablespace()) != null && database.supportsTablespaces()) {
            if (database instanceof MSSQLDatabase || database instanceof SybaseASADatabase) {
                buffer.append(" ON ").append(statement.getTablespace());
            } else if (database instanceof DB2Database || database instanceof InformixDatabase) {
                buffer.append(" IN ").append(statement.getTablespace());
            } else {
                buffer.append(" TABLESPACE ").append(statement.getTablespace());
            }
        }

        return new Sql[]{new UnparsedSql(buffer.toString())};
    }
}
