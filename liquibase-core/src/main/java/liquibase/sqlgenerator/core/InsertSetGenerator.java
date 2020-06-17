package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.InsertSetStatement;
import liquibase.statement.core.InsertStatement;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Table;

import java.util.ArrayList;
import java.util.SortedSet;

public class InsertSetGenerator extends AbstractSqlGenerator<InsertSetStatement> {

    @Override
    public ValidationErrors validate(InsertSetStatement insertStatementSet, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", insertStatementSet.peek().getTableName());
        validationErrors.checkRequiredField("columns", insertStatementSet.peek().getColumnValues());

        return validationErrors;
    }

    @Override
	public Sql[] generateSql(InsertSetStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {

		if (statement.peek() == null) {
			return new UnparsedSql[0];
		}
		StringBuffer sql = new StringBuffer();
		generateHeader(sql, statement, database);

		ArrayList<Sql> result = new ArrayList<>();
		int index = 0;
		for (InsertStatement sttmnt : statement.getStatements()) {
			index++;
			getInsertGenerator(database).generateValues(sql, sttmnt, database);
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
		getInsertGenerator(database).generateHeader(sql, insert, database);
	}

	protected InsertGenerator getInsertGenerator(Database database) {
		SortedSet<SqlGenerator> generators = SqlGeneratorFactory.getInstance().getGenerators(new InsertStatement(null, null, null), database);
		if ((generators == null) || generators.isEmpty()) {
			return null;
		}
		return (InsertGenerator) generators.iterator().next();
	}

	protected Relation getAffectedTable(InsertSetStatement statement) {
        return new Table().setName(statement.getTableName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }
}
