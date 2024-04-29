package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
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

        if ((addForeignKeyConstraintStatement.isInitiallyDeferred() || addForeignKeyConstraintStatement.isDeferrable())
                && !database.supportsInitiallyDeferrableColumns()
                ) {
            if(database.failOnDefferable()) {
                validationErrors.checkDisallowedField("initiallyDeferred", addForeignKeyConstraintStatement.isInitiallyDeferred(), database);
                validationErrors.checkDisallowedField("deferrable", addForeignKeyConstraintStatement.isDeferrable(), database);
            }else{
                // reset this as it's not supported
                addForeignKeyConstraintStatement.setDeferrable(false);
                addForeignKeyConstraintStatement.setInitiallyDeferred(false);
            }

        }

        validationErrors.checkRequiredField("baseColumnNames", addForeignKeyConstraintStatement.getBaseColumnNames());
        validationErrors.checkRequiredField("baseTableNames", addForeignKeyConstraintStatement.getBaseTableName());
        validationErrors.checkRequiredField("referencedColumnNames", addForeignKeyConstraintStatement.getReferencedColumnNames());
        validationErrors.checkRequiredField("referencedTableName", addForeignKeyConstraintStatement.getReferencedTableName());
        validationErrors.checkRequiredField("constraintName", addForeignKeyConstraintStatement.getConstraintName());

        validationErrors.checkDisallowedField("onDelete", addForeignKeyConstraintStatement.getOnDelete(), database, SybaseDatabase.class);

        if (database instanceof SybaseASADatabase) {
            validationErrors.addWarning("SQL Anywhere will apply RESTRICT instead of NO ACTION.");
        }

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
            } else if ((database instanceof MSSQLDatabase) && "RESTRICT".equalsIgnoreCase(statement.getOnUpdate())) {
                //don't use
            } else if (database instanceof InformixDatabase) {
                //TODO don't know if correct
            } else if ((database instanceof FirebirdDatabase) && "RESTRICT".equalsIgnoreCase(statement.getOnUpdate())) {
                //don't use
            } else if (database instanceof SybaseDatabase) {
                //don't use
            } else if ((database instanceof SybaseASADatabase) && "NO ACTION".equalsIgnoreCase(statement.getOnUpdate())) {
                //SQL Anywhere cannot do "nothing", so we let SQL Anywhere choose its implicit default (i.e. RESTRICT)
            } else {
                sb.append(" ON UPDATE ").append(statement.getOnUpdate());
            }
        }

        if (statement.getOnDelete() != null) {
            if ((database instanceof OracleDatabase) && ("RESTRICT".equalsIgnoreCase(statement.getOnDelete()) || ("NO " +
                    "ACTION").equalsIgnoreCase(statement.getOnDelete()))) {
                //don't use
            } else if ((database instanceof MSSQLDatabase) && "RESTRICT".equalsIgnoreCase(statement.getOnDelete())) {
                //don't use
            } else if ((database instanceof InformixDatabase) && !("CASCADE".equalsIgnoreCase(statement.getOnDelete()))) {
                //TODO Informix can handle ON DELETE CASCADE only, but I don't know if this is really correct
                // see "REFERENCES Clause" in manual
            } else if ((database instanceof FirebirdDatabase) && "RESTRICT".equalsIgnoreCase(statement.getOnDelete())) {
                //don't use
            } else if (database instanceof SybaseDatabase) {
                //don't use
            } else if ((database instanceof SybaseASADatabase) && "NO ACTION".equalsIgnoreCase(statement.getOnDelete())) {
                //SQL Anywhere cannot do "nothing", so we let SQL Anywhere choose its implicit default (i.e. RESTRICT)
            } else {
                sb.append(" ON DELETE ").append(statement.getOnDelete());
            }
        }

        if (statement.isDeferrable() || statement.isInitiallyDeferred()) {
            if (statement.isDeferrable() && !(database instanceof SybaseASADatabase)) {
                sb.append(" DEFERRABLE");
            }

            if (statement.isInitiallyDeferred()) {
                if (database instanceof SybaseASADatabase) {
                    sb.append(" CHECK ON COMMIT");
                } else {
                    sb.append(" INITIALLY DEFERRED");
                }
            }
        }

        if (database instanceof OracleDatabase) {
            sb.append(!statement.shouldValidate() ? " ENABLE NOVALIDATE " : "");
        }

        // since Postgres 9.1
        // https://www.postgresql.org/docs/9.1/sql-altertable.html
        if (database instanceof PostgresDatabase) {
            sb.append(!statement.shouldValidate() ? " NOT VALID " : "");
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
