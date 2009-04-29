package liquibase.database.statement.generator;

import liquibase.database.*;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;
import liquibase.database.statement.CreateIndexStatement;
import liquibase.util.StringUtils;

import java.util.Iterator;
import java.util.Arrays;

public class CreateIndexGenerator implements SqlGenerator<CreateIndexStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(CreateIndexStatement statement, Database database) {
        return true;
    }

    public GeneratorValidationErrors validate(CreateIndexStatement createIndexStatement, Database database) {
        return new GeneratorValidationErrors();
    }

    public Sql[] generateSql(CreateIndexStatement statement, Database database) {
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
