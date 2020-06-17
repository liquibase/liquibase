package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DropForeignKeyConstraintStatement;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Table;

public class DropForeignKeyConstraintGenerator extends AbstractSqlGenerator<DropForeignKeyConstraintStatement> {

    @Override
    public boolean supports(DropForeignKeyConstraintStatement statement, Database database) {
        return (!(database instanceof SQLiteDatabase));
    }

    @Override
    public ValidationErrors validate(DropForeignKeyConstraintStatement dropForeignKeyConstraintStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("baseTableName", dropForeignKeyConstraintStatement.getBaseTableName());
        validationErrors.checkRequiredField("constraintName", dropForeignKeyConstraintStatement.getConstraintName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DropForeignKeyConstraintStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        if ((database instanceof MySQLDatabase) || (database instanceof SybaseASADatabase)) {
            return new Sql[] { new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getBaseTableCatalogName(), statement.getBaseTableSchemaName(), statement.getBaseTableName()) + " DROP FOREIGN KEY " + database.escapeConstraintName(statement.getConstraintName()), getAffectedForeignKey(statement)) };
        } else {
            return new Sql[] { new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getBaseTableCatalogName(), statement.getBaseTableSchemaName(), statement.getBaseTableName()) + " DROP CONSTRAINT " + database.escapeConstraintName(statement.getConstraintName()), getAffectedForeignKey(statement)) };
        }

    }

    protected ForeignKey getAffectedForeignKey(DropForeignKeyConstraintStatement statement) {
        return new ForeignKey().setName(statement.getConstraintName()).setForeignKeyTable((Table) new Table().setName(statement.getBaseTableName()).setSchema(statement.getBaseTableCatalogName(), statement.getBaseTableSchemaName()));
    }
}
