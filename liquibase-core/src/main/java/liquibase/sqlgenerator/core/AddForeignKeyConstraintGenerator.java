package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.statement.core.AddForeignKeyConstraintStatement;

public class AddForeignKeyConstraintGenerator extends AbstractSqlGenerator<AddForeignKeyConstraintStatement> {

    @Override
    @SuppressWarnings({"SimplifiableIfStatement"})
    public boolean supports(AddForeignKeyConstraintStatement statement, ExecutionOptions options) {
        return (!(options.getRuntimeEnvironment().getTargetDatabase() instanceof SQLiteDatabase));
    }

    @Override
    public ValidationErrors validate(AddForeignKeyConstraintStatement addForeignKeyConstraintStatement, ExecutionOptions options, ActionGeneratorChain chain) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();
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
    public Action[] generateActions(AddForeignKeyConstraintStatement statement, ExecutionOptions options, ActionGeneratorChain chain) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();
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

	    return new Action[]{
			    new UnparsedSql(sb.toString())
	    };
    }
}
