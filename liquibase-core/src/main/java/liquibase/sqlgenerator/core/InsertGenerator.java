package liquibase.sqlgenerator.core;

import java.util.ArrayList;
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
import java.util.List;
import liquibase.database.core.OracleDatabase;

public class InsertGenerator extends AbstractSqlGenerator<InsertStatement> {

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
        StringBuffer sql = new StringBuffer("INSERT INTO " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " (");
        
        List<Object> columns = new ArrayList<Object>();
        List<Object> columnvalues = new ArrayList<Object>();
        
        List<String> largestr = new ArrayList<String>();
        int largeint = 0;
        
        for (String column : statement.getColumnValues().keySet()) {
            
            Object newValue = statement.getColumnValues().get(column);
            Object value = "#__NULL__#";
            
            if (newValue == null || newValue.toString().equalsIgnoreCase("NULL")) {
                
            } else if (newValue instanceof String && !looksLikeFunctionCall(((String) newValue), database)) {
                value = DataTypeFactory.getInstance().fromObject(newValue, database).objectToSql(newValue, database);
            } else if (newValue instanceof Date) {
                value = database.getDateLiteral(((Date) newValue));
            } else if (newValue instanceof Boolean) {
                if (((Boolean) newValue)) {
                    value = DataTypeFactory.getInstance().getTrueBooleanValue(database);
                } else {
                    value = DataTypeFactory.getInstance().getFalseBooleanValue(database);
                }
            } else if (newValue instanceof DatabaseFunction) {
                value = database.generateDatabaseFunctionValue((DatabaseFunction) newValue);
            }
            else {
                value = newValue;
            }
            
            if( !value.equals("#__NULL__#")  ){
               
                if(  String.valueOf(value).equals("''") ){
                    value = "' '";
                }
                
               if ( database instanceof OracleDatabase) {
                   if( String.valueOf(value).length()-2 > 4000 ){ // minus the two quotes
                       largestr.add(String.valueOf(value));                       
                       value = "__LARGE#CLOB#"+String.valueOf(largeint)+"";
                       largeint++;
                   }
               }
                
                columns.add(database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), column));
                columnvalues.add(value);
            }
            
        }
        
        if( columns.isEmpty() ){
            return null;
        }
        
         for (Object column : columns) {
            sql.append(column).append(", ");
        }
        sql.deleteCharAt(sql.lastIndexOf(" "));
        int lastComma = sql.lastIndexOf(",");
        if (lastComma >= 0) {
            sql.deleteCharAt(lastComma);
        }
        
        sql.append(") VALUES (");

        for (Object column : columnvalues) {
            sql.append(column).append(", ");
        }
        sql.deleteCharAt(sql.lastIndexOf(" "));
        lastComma = sql.lastIndexOf(",");
        if (lastComma >= 0) {
            sql.deleteCharAt(lastComma);
        }
        
        sql.append(")");
        
        if( largeint > 0 ){
            
            String query = "DECLARE\n__DECLARE#TEXT#";
            String declaretxt = "";
            
            for( int x=0; x<largeint; x++ ){
                declaretxt += "clobtext"+String.valueOf(x)+" CLOB;\n";
            }
            
                query += "BEGIN\n";
                String newsql = sql.toString();
            
            for( int x=0; x<largeint; x++ ){
                
                if( largestr.get(x).length() > 30000 ){
                    
                    String clobtext = "clobtext"+String.valueOf(x);
                    String clobtextis = clobtext+" := "+clobtext+"subclob0";
                    String substr = clobtext+"subclob0 := ";
                    declaretxt += clobtext+"subclob0 CLOB;\n";
                    StringBuffer newsubstr = new StringBuffer( largestr.get(x) );
                    int a = 1;
                    for( int i=0; i<largestr.get(x).length(); i++ ){
                        
                        substr += newsubstr.charAt(i);
                        if( i>0 && i%30000==0 ){
                            substr += "';\n"+clobtext+"subclob"+String.valueOf(a)+" := '";
                            declaretxt += clobtext+"subclob"+String.valueOf(a)+" CLOB;\n";
                            clobtextis += " || "+clobtext+"subclob"+String.valueOf(a);
                            a++;
                        }
                        
                    }
                    query += substr + ";\n"+clobtextis+";\n";                    
                    
                }else{
                    
                    query += "clobtext"+String.valueOf(x)+" := "+largestr.get(x)+";\n";
                    
                }
                
                query = query.replace( "__DECLARE#TEXT#", declaretxt );                
                newsql = newsql.replace( "__LARGE#CLOB#"+String.valueOf(x), "clobtext"+String.valueOf(x) );
                
            }
            
            query += newsql + ";\nEND;";
            
            return new Sql[] {
                    new UnparsedSql(query, getAffectedTable(statement))
            };
            
        
        }

        return new Sql[] {
                new UnparsedSql(sql.toString(), getAffectedTable(statement))
        };
    }

    protected Relation getAffectedTable(InsertStatement statement) {
        return new Table().setName(statement.getTableName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }
}
