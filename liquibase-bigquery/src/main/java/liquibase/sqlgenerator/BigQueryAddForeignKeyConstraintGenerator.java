package liquibase.sqlgenerator;

import liquibase.database.BigQueryDatabase;
import liquibase.database.Database;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.core.AddForeignKeyConstraintGenerator;
import liquibase.statement.core.AddForeignKeyConstraintStatement;

public class BigQueryAddForeignKeyConstraintGenerator extends AddForeignKeyConstraintGenerator {

    @Override
    public int getPriority() {
        return SqlGenerator.PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddForeignKeyConstraintStatement statement, Database database) {
        return database instanceof BigQueryDatabase;
    }

    @Override
    public Sql[] generateSql(AddForeignKeyConstraintStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE ")
                .append(database.escapeTableName(statement.getBaseTableCatalogName(), statement.getBaseTableSchemaName(), statement.getBaseTableName()))
                .append(" ADD CONSTRAINT ")
                .append(database.escapeConstraintName(statement.getConstraintName()))
                .append(" FOREIGN KEY (")
                .append(database.escapeColumnNameList(statement.getBaseColumnNames()))
                .append(") REFERENCES ")
                .append(database.escapeTableName(statement.getReferencedTableCatalogName(), statement.getReferencedTableSchemaName(),
                        statement.getReferencedTableName()))
                .append(" (")
                .append(database.escapeColumnNameList(statement.getReferencedColumnNames()))
                .append(") NOT ENFORCED");

        return new Sql[]{
                new UnparsedSql(sb.toString(), getAffectedForeignKey(statement))
        };
    }
}
