package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddForeignKeyConstraintStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Table;

public class AddForeignKeyConstraintGenerator extends AbstractSqlGenerator<AddForeignKeyConstraintStatement> {

    @Override
    @SuppressWarnings({"SimplifiableIfStatement"})
    public boolean supports(AddForeignKeyConstraintStatement statement, Database database) {
        return (!(database instanceof SQLiteDatabase));
    }

    @Override
    public ValidationErrors validate(AddForeignKeyConstraintStatement addForeignKeyConstraintStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();

        if ((addForeignKeyConstraintStatement.isInitiallyDeferred() || addForeignKeyConstraintStatement.isDeferrable()) && !database.supportsInitiallyDeferrableColumns()) {
            validationErrors.checkDisallowedField("initiallyDeferred", addForeignKeyConstraintStatement.isInitiallyDeferred(), database);
            validationErrors.checkDisallowedField("deferrable", addForeignKeyConstraintStatement.isDeferrable(), database);
        }

        validationErrors.checkRequiredField("baseColumnNames", addForeignKeyConstraintStatement.getBaseColumnNames());
        validationErrors.checkRequiredField("baseTableNames", addForeignKeyConstraintStatement.getBaseTableName());
        validationErrors.checkRequiredField("referencedColumnNames", addForeignKeyConstraintStatement.getReferencedColumnNames());
        validationErrors.checkRequiredField("referencedTableName", addForeignKeyConstraintStatement.getReferencedTableName());
        validationErrors.checkRequiredField("constraintName", addForeignKeyConstraintStatement.getConstraintName());

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(AddForeignKeyConstraintStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("ALTER TABLE ")
			    .append(database.escapeTableName(statement.getBaseTableCatalogName(), statement.getBaseTableSchemaName(), statement.getBaseTableName()))
			    .append(" ADD CONSTRAINT ");
	    if (!(database instanceof InformixDatabase)) {
		    sb.append(database.escapeConstraintName(statement.getConstraintName()));
	    }
	    sb.append(" FOREIGN KEY (")
			    .append(database.escapeColumnNameList(statement.getBaseColumnNames()))
			    .append(") REFERENCES ")
			    .append(database.escapeTableName(statement.getReferencedTableCatalogName(), statement.getReferencedTableSchemaName(), statement.getReferencedTableName()))
			    .append(" (")
			    .append(database.escapeColumnNameList(statement.getReferencedColumnNames()))
			    .append(")");

	    if (statement.getOnUpdate() != null) {
		    if (database instanceof OracleDatabase) {
			    //don't use
		    } else if (database instanceof InformixDatabase) {
			    //TODO don't know if correct
		    } else {
			    sb.append(" ON UPDATE ").append(statement.getOnUpdate());
		    }
	    }

	    if (statement.getOnDelete() != null) {
            if ((database instanceof OracleDatabase) && (statement.getOnDelete().equalsIgnoreCase("RESTRICT") || statement.getOnDelete().equalsIgnoreCase("NO ACTION"))) {
                //don't use
            } else if ((database instanceof MSSQLDatabase) && statement.getOnDelete().equalsIgnoreCase("RESTRICT")) {
                //don't use                        
		    } else if (database instanceof InformixDatabase && !(statement.getOnDelete().equalsIgnoreCase("CASCADE"))) {
			    //TODO Informix can handle ON DELETE CASCADE only, but I don't know if this is really correct
		    	// see "REFERENCES Clause" in manual
		    } else {
			    sb.append(" ON DELETE ").append(statement.getOnDelete());
		    }
	    }

        if (statement.isDeferrable() || statement.isInitiallyDeferred()) {
            if (statement.isDeferrable()) {
                sb.append(" DEFERRABLE");
            }

            if (statement.isInitiallyDeferred()) {
                sb.append(" INITIALLY DEFERRED");
            }
        }

	    if (database instanceof InformixDatabase) {
		    sb.append(" CONSTRAINT ");
		    sb.append(database.escapeConstraintName(statement.getConstraintName()));
	    }

	    return new Sql[]{
			    new UnparsedSql(sb.toString(), getAffectedForeignKey(statement))
	    };
    }

    protected ForeignKey getAffectedForeignKey(AddForeignKeyConstraintStatement statement) {
        return new ForeignKey().setName(statement.getConstraintName()).setForeignKeyColumns(Column.listFromNames(statement.getBaseColumnNames())).setForeignKeyTable((Table) new Table().setName(statement.getBaseTableName()).setSchema(statement.getBaseTableCatalogName(), statement.getBaseTableSchemaName()));
    }
}
