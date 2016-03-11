package liquibase.sqlgenerator.core;

import java.util.ArrayList;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.InsertSetStatement;
import liquibase.statement.core.InsertStatement;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Table;

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

		if (statement.peek() == null) {
			return new UnparsedSql[0];
		}
		StringBuffer sql = new StringBuffer();
		generateHeader(sql, statement, database);

		ArrayList<Sql> result = new ArrayList<Sql>();
		int index = 0;
		for (InsertStatement sttmnt : statement.getStatements()) {
			index++;
			((InsertGenerator) SqlGeneratorFactory.getInstance().getBestGenerator(sttmnt, database)).generateValues(sql, sttmnt, database);
			sql.append(",");
			if (index > statement.getBatchThreshold()) {
				result.add(completeStatement(statement, sql));

				index = 0;
				sql = new StringBuffer();
				generateHeader(sql, statement, database);
			}
		}
		if (index > 0) {
			result.add(completeStatement(statement, sql));
		}

		return result.toArray(new UnparsedSql[result.size()]);
	}
    
	private Sql completeStatement(InsertSetStatement statement, StringBuffer sql) {
		sql.deleteCharAt(sql.lastIndexOf(","));
		sql.append(";\n");
		return new UnparsedSql(sql.toString(), getAffectedTable(statement));
	}
    
    public void generateHeader(StringBuffer sql,InsertSetStatement statement, Database database) {
        InsertStatement insert=statement.peek();
		((InsertGenerator) SqlGeneratorFactory.getInstance().getBestGenerator(statement, database)).generateHeader(sql,insert,database);
    }
    
    protected Relation getAffectedTable(InsertSetStatement statement) {
        return new Table().setName(statement.getTableName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }
}
