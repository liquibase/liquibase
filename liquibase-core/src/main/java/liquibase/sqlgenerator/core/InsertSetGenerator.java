package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutorService;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.InsertGenerator;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.core.InsertStatement;
import liquibase.statement.core.InsertSetStatement;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Table;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InsertSetGenerator extends AbstractSqlGenerator<InsertSetStatement> {

    @Override
    public ValidationErrors validate(InsertSetStatement insertStatementSet, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", insertStatementSet.peek().getTableName());
        validationErrors.checkRequiredField("columns", insertStatementSet.peek().getColumnValues());

//      it is an error if any of the individual statements have a different table,schema, or catalog.

//        if (insertStatement.getSchemaName() != null && !database.supportsSchemas()) {
//           validationErrors.addError("Database does not support schemas");
//       }

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(InsertSetStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {

    	if(statement.peek() == null) {
    		return new UnparsedSql[0];
    	}
        StringBuffer sql = new StringBuffer();
        
        generateHeader(sql,statement,database);
        generateValues(sql,statement,database);

        return new Sql[] {
                new UnparsedSql(sql.toString(), getAffectedTable(statement))
        };
    }
    
    public void generateHeader(StringBuffer sql,InsertSetStatement statement, Database database) {
        InsertStatement insert=statement.peek();
        myGenerator.generateHeader(sql,insert,database);
    }
    
    public void generateValues(StringBuffer sql,InsertSetStatement statements, Database database) {
        int index = 0;
        for( InsertStatement statement : statements.getStatements()) {
          index++;
          if(index>statements.getBatchThreshold()) {
            sql.deleteCharAt(sql.lastIndexOf(","));
            sql.append(";\n");            
            generateHeader(sql,statements,database);
            index=0;
          }
          myGenerator.generateValues(sql,statement,database);
          sql.append(",");
        }
        sql.deleteCharAt(sql.lastIndexOf(","));
        sql.append(";");
    }

    protected Relation getAffectedTable(InsertSetStatement statement) {
        return new Table().setName(statement.getTableName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }
    
    private InsertGenerator myGenerator= new InsertGenerator();
}
