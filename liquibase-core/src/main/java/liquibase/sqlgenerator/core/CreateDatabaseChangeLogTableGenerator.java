package liquibase.sqlgenerator.core;

import liquibase.changelog.definition.ChangeLogColumnDefinition;
import liquibase.changelog.definition.ChangeLogTableDefinition;
import liquibase.database.Database;
import liquibase.database.core.SybaseDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.ColumnConstraint;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;
import liquibase.statement.core.CreateTableStatement;

public class CreateDatabaseChangeLogTableGenerator extends AbstractSqlGenerator<CreateDatabaseChangeLogTableStatement> {

    @Override
    public boolean supports(CreateDatabaseChangeLogTableStatement statement, Database database) {
        return !(database instanceof SybaseDatabase);
    }

    @Override
    public ValidationErrors validate(CreateDatabaseChangeLogTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public Sql[] generateSql(CreateDatabaseChangeLogTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        CreateTableStatement createTableStatement = new CreateTableStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                .setTablespace(database.getLiquibaseTablespaceName());

        ChangeLogTableDefinition definition = statement.getDefinition();
        for(ChangeLogColumnDefinition columnDefinition : definition.getColumnDefinitions().values()) {
            createTableStatement.addColumn(
                    columnDefinition.getColumnName(),
                    columnDefinition.getDataType(),
                    columnDefinition.getDefaultValue(),
                    columnDefinition.getRemarks(),
                    columnDefinition.getConstraints().toArray(new ColumnConstraint[] {})
            );
        }
        return SqlGeneratorFactory.getInstance().generateSql(createTableStatement, database);
    }

}
