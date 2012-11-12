package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.InsertOrUpdateStatement;
import liquibase.statement.core.UpdateStatement;

import java.util.Date;

public class InsertOrUpdateGeneratorOracle extends InsertOrUpdateGenerator {


    @Override
    public boolean supports(InsertOrUpdateStatement statement, Database database) {
        return database instanceof OracleDatabase;
    }

    @Override
    protected String getRecordCheck(InsertOrUpdateStatement insertOrUpdateStatement, Database database, String whereClause) {

        StringBuffer recordCheckSql = new StringBuffer();

        recordCheckSql.append("DECLARE\n");
        recordCheckSql.append("\tv_reccount NUMBER := 0;\n");
        recordCheckSql.append("BEGIN\n");
        recordCheckSql.append("\tSELECT COUNT(*) INTO v_reccount FROM " + database.escapeTableName(insertOrUpdateStatement.getSchemaName(), insertOrUpdateStatement.getTableName()) + " WHERE ");

        recordCheckSql.append(whereClause);
        recordCheckSql.append(";\n");

        recordCheckSql.append("\tIF v_reccount = 0 THEN\n");

        return recordCheckSql.toString();
    }

    @Override
    protected String getElse(Database database){
               return "\tELSIF v_reccount = 1 THEN\n";
    }

    @Override
    protected String getPostUpdateStatements(Database database){
        StringBuffer endStatements = new StringBuffer();
        endStatements.append("END IF;\n");
        endStatements.append("END;\n");
        if ( database.getGenerateSqlForFileOutput() == true ) {
        	endStatements.append("/\n");
        }
        return endStatements.toString();

    }
}
