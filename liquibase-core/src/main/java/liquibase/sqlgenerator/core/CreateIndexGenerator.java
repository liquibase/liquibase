package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.structure.core.Index;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateIndexStatement;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CreateIndexGenerator extends AbstractSqlGenerator<CreateIndexStatement> {

    @Override
    public ValidationErrors validate(CreateIndexStatement createIndexStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", createIndexStatement.getTableName());
        validationErrors.checkRequiredField("columns", createIndexStatement.getColumns());
        if (database instanceof HsqlDatabase) {
            validationErrors.checkRequiredField("name", createIndexStatement.getIndexName());
        }
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(CreateIndexStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {

	    if (database instanceof OracleDatabase) {
		    // Oracle don't create index when creates foreignKey
		    // It means that all indexes associated with foreignKey should be created manualy
		    List<String> associatedWith = StringUtils.splitAndTrim(statement.getAssociatedWith(), ",");
		    if (associatedWith != null && (associatedWith.contains(Index.MARK_PRIMARY_KEY) || associatedWith.contains(Index.MARK_UNIQUE_CONSTRAINT))) {
			    return new Sql[0];
		    }
	    } else {
		    // Default filter of index creation:
		    // creation of all indexes with associations are switched off.
		    List<String> associatedWith = StringUtils.splitAndTrim(statement.getAssociatedWith(), ",");
		    if (associatedWith != null && (associatedWith.contains(Index.MARK_PRIMARY_KEY) ||
		        associatedWith.contains(Index.MARK_UNIQUE_CONSTRAINT) ||
				associatedWith.contains(Index.MARK_FOREIGN_KEY))) {
			    return new Sql[0];
		    }
	    }

	    StringBuffer buffer = new StringBuffer();

	    buffer.append("CREATE ");
	    if (statement.isUnique() != null && statement.isUnique()) {
		    buffer.append("UNIQUE ");
	    }
	    buffer.append("INDEX ");

	    if (statement.getIndexName() != null) {
            String indexSchema = statement.getTableSchemaName();
            buffer.append(database.escapeIndexName(statement.getTableCatalogName(), indexSchema, statement.getIndexName())).append(" ");
	    }
	    buffer.append("ON ");
	    buffer.append(database.escapeTableName(statement.getTableCatalogName(), statement.getTableSchemaName(), statement.getTableName())).append("(");
	    Iterator<String> iterator = Arrays.asList(statement.getColumns()).iterator();
	    while (iterator.hasNext()) {
		    String column = iterator.next();
		    buffer.append(database.escapeColumnName(statement.getTableCatalogName(), statement.getTableSchemaName(), statement.getTableName(), column));
		    if (iterator.hasNext()) {
			    buffer.append(", ");
		    }
	    }
	    buffer.append(")");

	    if (StringUtils.trimToNull(statement.getTablespace()) != null && database.supportsTablespaces()) {
		    if (database instanceof MSSQLDatabase || database instanceof SybaseASADatabase) {
			    buffer.append(" ON ").append(statement.getTablespace());
		    } else if (database instanceof DB2Database || database instanceof InformixDatabase) {
			    buffer.append(" IN ").append(statement.getTablespace());
		    } else {
			    buffer.append(" TABLESPACE ").append(statement.getTablespace());
		    }
	    }

	    return new Sql[]{new UnparsedSql(buffer.toString())};
    }
}
