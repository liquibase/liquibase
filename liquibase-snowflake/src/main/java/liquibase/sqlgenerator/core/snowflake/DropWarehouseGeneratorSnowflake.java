package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.snowflake.DropWarehouseStatement;

public class DropWarehouseGeneratorSnowflake extends AbstractSqlGenerator<DropWarehouseStatement> {

    @Override
    public boolean supports(DropWarehouseStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(DropWarehouseStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors errors = new ValidationErrors();
        
        if (statement.getWarehouseName() == null || statement.getWarehouseName().trim().isEmpty()) {
            errors.addError("Warehouse name is required");
        }
        
        return errors;
    }

    @Override
    public Sql[] generateSql(DropWarehouseStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder sql = new StringBuilder();
        sql.append("DROP WAREHOUSE ");
        
        if (Boolean.TRUE.equals(statement.getIfExists())) {
            sql.append("IF EXISTS ");
        }
        
        sql.append(database.escapeObjectName(statement.getWarehouseName(), liquibase.structure.core.Table.class));
        
        return new Sql[]{new UnparsedSql(sql.toString())};
    }
}