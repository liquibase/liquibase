package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddPrimaryKeyStatement;

public class AddPrimaryKeyGeneratorInformix extends AddPrimaryKeyGenerator {
	
    @Override
    public int getPriority() {
        return SqlGenerator.PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddPrimaryKeyStatement statement, Database database) {
        return (database instanceof InformixDatabase);
    }
    
    @Override
    public Sql[] generateSql(AddPrimaryKeyStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder sql = new StringBuilder();
        sql.append("ALTER TABLE ");
        sql.append(database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()));
        sql.append(" ADD CONSTRAINT PRIMARY KEY (");
        sql.append(database.escapeColumnNameList(statement.getColumnNames()));
        sql.append(")");
   	    if (statement.getConstraintName() != null) {
   	        sql.append(" CONSTRAINT ");
   	        sql.append(database.escapeConstraintName(statement.getConstraintName()));
   	    }

        return new Sql[] {
                new UnparsedSql(sql.toString(), getAffectedPrimaryKey(statement))
        };
    }
}
