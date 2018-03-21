package liquibase.sqlgenerator.core;

import liquibase.change.AddColumnConfig;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.sdk.database.MockDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateIndexStatement;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CreateIndexGenerator extends AbstractSqlGenerator<CreateIndexStatement> {

    @Override
    public ValidationErrors validate(CreateIndexStatement createIndexStatement, Database database,
                                     SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", createIndexStatement.getTableName());
        validationErrors.checkRequiredField("columns", createIndexStatement.getColumns());
        if (database instanceof HsqlDatabase) {
            validationErrors.checkRequiredField("name", createIndexStatement.getIndexName());
        }
        return validationErrors;
    }

    @Override
    public Warnings warn(CreateIndexStatement createIndexStatement, Database database,
                         SqlGeneratorChain sqlGeneratorChain) {

        Warnings warnings = super.warn(createIndexStatement, database, sqlGeneratorChain);
        if (!((database instanceof MSSQLDatabase) || (database instanceof OracleDatabase) || (database instanceof
                AbstractDb2Database) || (database instanceof PostgresDatabase) || (database instanceof MockDatabase))) {
            if ((createIndexStatement.isClustered() != null) && createIndexStatement.isClustered()) {
                warnings.addWarning("Creating clustered index not supported with "+database);
            }
        }

        return warnings;
    }


    /**
     * Generate a CREATE INDEX SQL statement.
     * Here, we are walking on thin ice, because the SQL Foundation standard (ISO/IEC 9075-2) does not concern itself
     * with indexes at all and leaves them as an implementation-specific detail at the discretion of each RDBMS vendor.
     * However, there is some common ground to most RDBMS, and we try to make an educated guess on how a CREATE INDEX
     * statement might look like if we have no specific handler for the DBMS.
     * @param statement A CreateIndexStatement with the desired properties of the SQL to be generated
     * @param database The DBMS for whose SQL dialect the statement is to be made
     * @param sqlGeneratorChain The other SQL generators in the same chain (but this method is not interested in them)
     * @return An array of Sql objects containing the generated SQL statement(s).
     */
    @Override
    public Sql[] generateSql(CreateIndexStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain)
    {
	    if (database instanceof OracleDatabase) {
		    /*
		     * Oracle automatically creates indexes for PRIMARY KEY and UNIQUE constraints, but does not do so
	         * for FOREIGN KEY constraints, though it is highly recommended to do to avoid potentially severe
	         *  performance problems when deleting rows from the parent table or changing the key column(s) in the
	         *  parent table.
		      */
		    List<String> associatedWith = StringUtils.splitAndTrim(statement.getAssociatedWith(), ",");
		    if ((associatedWith != null) && (associatedWith.contains(Index.MARK_PRIMARY_KEY) || associatedWith
                .contains(Index.MARK_UNIQUE_CONSTRAINT))) {
			    return new Sql[0];
		    }
	    } else {
		    // Default filter of index creation:
		    // creation of all indexes with associations are switched off.
		    List<String> associatedWith = StringUtils.splitAndTrim(statement.getAssociatedWith(), ",");
		    if ((associatedWith != null) && (associatedWith.contains(Index.MARK_PRIMARY_KEY) || associatedWith
                .contains(Index.MARK_UNIQUE_CONSTRAINT) || associatedWith.contains(Index.MARK_FOREIGN_KEY))) {
			    return new Sql[0];
		    }
	    }

	    StringBuffer buffer = new StringBuffer();

	    buffer.append("CREATE ");
	    if ((statement.isUnique() != null) && statement.isUnique()) {
		    buffer.append("UNIQUE ");
	    }


        if (database instanceof MSSQLDatabase) {
            if (statement.isClustered() != null) {
                if (statement.isClustered()) {
                    buffer.append("CLUSTERED ");
                } else {
                    buffer.append("NONCLUSTERED ");
                }
            }
        }

        buffer.append("INDEX ");

	    if (statement.getIndexName() != null) {
            String indexSchema = statement.getTableSchemaName();
            buffer.append(database.escapeIndexName(statement.getTableCatalogName(), indexSchema, statement.getIndexName())).append(" ");
	    }
	    buffer.append("ON ");
        if ((database instanceof OracleDatabase) && (statement.isClustered() != null) && statement.isClustered()){
            buffer.append("CLUSTER ");
        }
	    buffer.append(database.escapeTableName(statement.getTableCatalogName(), statement.getTableSchemaName(), statement.getTableName())).append("(");
	    Iterator<AddColumnConfig> iterator = Arrays.asList(statement.getColumns()).iterator();
	    while (iterator.hasNext()) {
            AddColumnConfig column = iterator.next();
            if (column.getComputed() == null) {
                buffer.append(database.escapeColumnName(statement.getTableCatalogName(), statement.getTableSchemaName(), statement.getTableName(), column.getName(), false));
            } else {
                if (column.getComputed()) {
                    buffer.append(column.getName());
                } else {
                    buffer.append(database.escapeColumnName(statement.getTableCatalogName(), statement.getTableSchemaName(), statement.getTableName(), column.getName()));
                }
            }
            if ((column.getDescending() != null) && column.getDescending()) {
                buffer.append(" DESC");
            }
            if (iterator.hasNext()) {
			    buffer.append(", ");
		    }
	    }
	    buffer.append(")");

	    if ((StringUtils.trimToNull(statement.getTablespace()) != null) && database.supportsTablespaces()) {
		    if ((database instanceof MSSQLDatabase) || (database instanceof SybaseASADatabase)) {
			    buffer.append(" ON ").append(statement.getTablespace());
		    } else if ((database instanceof AbstractDb2Database) || (database instanceof InformixDatabase)) {
			    buffer.append(" IN ").append(statement.getTablespace());
		    } else {
			    buffer.append(" TABLESPACE ").append(statement.getTablespace());
		    }
	    }

        if ((database instanceof AbstractDb2Database) && (statement.isClustered() != null) && statement.isClustered()){
            buffer.append(" CLUSTER");
        }

        return new Sql[] {new UnparsedSql(buffer.toString(), getAffectedIndex(statement))};
    }

    protected Index getAffectedIndex(CreateIndexStatement statement) {
        return new Index().setName(statement.getIndexName()).setTable((Table) new Table().setName(statement.getTableName()).setSchema(statement.getTableCatalogName(), statement.getTableSchemaName()));
    }
}
