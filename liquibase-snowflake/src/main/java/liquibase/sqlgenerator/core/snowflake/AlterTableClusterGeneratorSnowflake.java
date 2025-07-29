package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.AlterTableClusterStatement;
import liquibase.structure.core.Table;

/**
 * SQL generator for Snowflake ALTER TABLE clustering operations.
 */
public class AlterTableClusterGeneratorSnowflake extends AbstractSqlGenerator<AlterTableClusterStatement> {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AlterTableClusterStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(AlterTableClusterStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();

        if (statement.getTableName() == null) {
            validationErrors.addError("tableName is required");
        }

        // Validate mutual exclusivity
        int operationCount = 0;
        if (statement.getClusterBy() != null) operationCount++;
        if (Boolean.TRUE.equals(statement.getDropClusteringKey())) operationCount++;
        if (Boolean.TRUE.equals(statement.getSuspendRecluster())) operationCount++;
        if (Boolean.TRUE.equals(statement.getResumeRecluster())) operationCount++;

        if (operationCount == 0) {
            validationErrors.addError("At least one clustering operation must be specified");
        } else if (operationCount > 1) {
            validationErrors.addError("Only one clustering operation allowed per statement");
        }

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(AlterTableClusterStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String tableName = database.escapeTableName(statement.getCatalogName(), 
                                                    statement.getSchemaName(), 
                                                    statement.getTableName());
        
        StringBuilder sql = new StringBuilder();
        sql.append("ALTER TABLE ").append(tableName);
        
        if (statement.getClusterBy() != null) {
            sql.append(" CLUSTER BY (").append(statement.getClusterBy()).append(")");
        } else if (Boolean.TRUE.equals(statement.getDropClusteringKey())) {
            sql.append(" DROP CLUSTERING KEY");
        } else if (Boolean.TRUE.equals(statement.getSuspendRecluster())) {
            sql.append(" SUSPEND RECLUSTER");
        } else if (Boolean.TRUE.equals(statement.getResumeRecluster())) {
            sql.append(" RESUME RECLUSTER");
        }
        
        return new Sql[]{new UnparsedSql(sql.toString(), getAffectedTable(statement))};
    }

    protected Table getAffectedTable(AlterTableClusterStatement statement) {
        return new Table(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName());
    }
}