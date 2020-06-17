package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.core.InsertStatement;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Table;

import java.util.Date;

public class InsertGenerator extends AbstractSqlGenerator<InsertStatement> {

	private boolean previousInsertHasHeader;
    @Override
    public ValidationErrors validate(InsertStatement insertStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", insertStatement.getTableName());
        validationErrors.checkRequiredField("columns", insertStatement.getColumnValues());

//        if (insertStatement.getSchemaName() != null && !database.supportsSchemas()) {
//           validationErrors.addError("Database does not support schemas");
//       }

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(InsertStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
       
        StringBuffer sql = new StringBuffer();
        
        if(!previousInsertHasHeader) {
        	generateHeader(sql,statement,database);
        } else {
            sql.append(",");        	
        }
        generateValues(sql,statement,database);

        return new Sql[] {
                new UnparsedSql(sql.toString(), getAffectedTable(statement))
        };
    }
    
    public void setPreviousInsertStatement(boolean previousInsertHasHeader) {
    	this.previousInsertHasHeader = previousInsertHasHeader;
    }
    
    public void generateHeader(StringBuffer sql,InsertStatement statement, Database database) {
        sql.append("INSERT INTO " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " (");
        for (String column : statement.getColumnValues().keySet()) {
            sql.append(database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), column)).append(", ");
        }
        sql.deleteCharAt(sql.lastIndexOf(" "));
        int lastComma = sql.lastIndexOf(",");
        if (lastComma >= 0) {
            sql.deleteCharAt(lastComma);
        }

        sql.append(") VALUES ");
    }
    public void generateValues(StringBuffer sql,InsertStatement statement, Database database) {
        sql.append("(");

        for (String column : statement.getColumnValues().keySet()) {
            Object newValue = statement.getColumnValues().get(column);
            if ((newValue == null) || "NULL".equalsIgnoreCase(newValue.toString())) {
                sql.append("NULL");
            } else if ((newValue instanceof String) && !looksLikeFunctionCall(((String) newValue), database)) {
                sql.append(DataTypeFactory.getInstance().fromObject(newValue, database).objectToSql(newValue, database));
            } else if (newValue instanceof Date) {
                sql.append(database.getDateLiteral(((Date) newValue)));
            } else if (newValue instanceof Boolean) {
                if (((Boolean) newValue)) {
                    sql.append(DataTypeFactory.getInstance().getTrueBooleanValue(database));
                } else {
                    sql.append(DataTypeFactory.getInstance().getFalseBooleanValue(database));
                }
            } else if (newValue instanceof DatabaseFunction) {
                sql.append(database.generateDatabaseFunctionValue((DatabaseFunction) newValue));
            }
            else {
                sql.append(newValue);
            }
            sql.append(", ");
        }

        sql.deleteCharAt(sql.lastIndexOf(" "));
        int lastComma = sql.lastIndexOf(",");
        if (lastComma >= 0) {
            sql.deleteCharAt(lastComma);
        }

        sql.append(")");
        
        
    }


    protected Relation getAffectedTable(InsertStatement statement) {
        return new Table().setName(statement.getTableName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }
}
