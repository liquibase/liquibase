package liquibase.sqlgenerator.core;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.DB2Database.DataServerType;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.FindForeignKeyConstraintsStatement;

public class FindForeignKeyConstraintsGeneratorDB2 extends AbstractSqlGenerator<FindForeignKeyConstraintsStatement> {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(FindForeignKeyConstraintsStatement statement, Database database) {
        return database instanceof DB2Database;
    }

    @Override
    public ValidationErrors validate(FindForeignKeyConstraintsStatement findForeignKeyConstraintsStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("baseTableName", findForeignKeyConstraintsStatement.getBaseTableName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(FindForeignKeyConstraintsStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder sb = new StringBuilder();

        if (((DB2Database)database).getDataServerType() == DataServerType.DB2Z){
        	CatalogAndSchema baseTableSchema = new CatalogAndSchema(statement.getBaseTableCatalogName(), statement.getBaseTableSchemaName()).customize(database);

        	sb.append("SELECT PK.TBNAME    as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_NAME).append(",");
			sb.append("       PK.NAME      as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_COLUMN_NAME).append(",");
			sb.append("       FK.TBNAME    as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_TABLE_NAME).append(",");
			sb.append("       FK.COLNAME   as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_COLUMN_NAME).append(",");
			sb.append("       FK.CONSTNAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_CONSTRAINT_NAME).append(" ");
			sb.append("  FROM SYSIBM.SYSRELS R,");
			sb.append("       SYSIBM.SYSFOREIGNKEYS FK,");
			sb.append("       SYSIBM.SYSCOLUMNS PK");
			sb.append(" WHERE R.CREATOR = '").append(baseTableSchema.getSchemaName()).append("'");
			sb.append("   AND R.TBNAME  = '").append(statement.getBaseTableName()).append("'");
			sb.append("   AND R.RELNAME = FK.RELNAME ");
			sb.append("   AND R.CREATOR=FK.CREATOR ");
			sb.append("   AND R.TBNAME=FK.TBNAME ");
			sb.append("   AND R.REFTBCREATOR=PK.TBCREATOR ");
			sb.append("   AND R.REFTBNAME=PK.TBNAME ");
			sb.append("   AND FK.COLSEQ=PK.KEYSEQ ");
			sb.append(" ORDER BY R.RELNAME, FK.COLSEQ asc ");
        }
		else {
			sb.append("SELECT ");
			sb.append("TABNAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_NAME).append(", ");
			sb.append("PK_COLNAMES as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_COLUMN_NAME).append(", ");
			sb.append("REFTABNAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_TABLE_NAME).append(", ");
			sb.append("FK_COLNAMES as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_COLUMN_NAME).append(",");
			sb.append("CONSTNAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_CONSTRAINT_NAME).append(" ");
			sb.append("FROM SYSCAT.REFERENCES ");
			sb.append("WHERE TABNAME='").append(statement.getBaseTableName()).append("'");
		}
        
        return new Sql[]{
                new UnparsedSql(sb.toString())
        };
    }
}