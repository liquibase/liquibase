package liquibase.sqlgenerator.core;

import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.SetNullableStatement;
import liquibase.structure.core.*;

public class SetNullableGeneratorSQLite extends AbstractSqlGenerator<SetNullableStatement> {

    @Override
    public boolean generateStatementsIsVolatile(Database database) {
        return true;
    }

    @Override
    public boolean supports(SetNullableStatement statement, Database database) {
        return database instanceof SQLiteDatabase;
    }

    @Override
    public ValidationErrors validate(SetNullableStatement setNullableStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("tableName", setNullableStatement.getTableName());
        validationErrors.checkRequiredField("columnName", setNullableStatement.getColumnName());

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(SetNullableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        try {
            Column columnSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(new Column().setName(statement.getColumnName()).setRelation(new Table().setName(statement.getTableName()).setSchema(new Schema(new Catalog(null), null))), database);

            ColumnConfig newColumnConfig = new ColumnConfig(columnSnapshot);
            if (newColumnConfig.getConstraints() == null) {
                newColumnConfig.setConstraints(new ConstraintsConfig());
            }
            newColumnConfig.getConstraints().setNullable(statement.isNullable());
            SQLiteDatabase.AlterTableVisitor alterTableVisitor = new SQLiteDatabase.AlterTableVisitor() {
                @Override
                public ColumnConfig[] getColumnsToAdd() {
                    return new ColumnConfig[]{newColumnConfig};
                }

                @Override
                public boolean copyThisColumn(ColumnConfig column) {
                    return !column.getName().equals(newColumnConfig.getName()) || column == newColumnConfig;
                }

                @Override
                public boolean createThisColumn(ColumnConfig column) {
                    return !column.getName().equals(newColumnConfig.getName()) || column == newColumnConfig;
                }

                @Override
                public boolean createThisIndex(Index index) {
                    return true;
                }
            };

            Sql[] generatedSqls = SQLiteDatabase.getAlterTableSqls(database, alterTableVisitor, statement.getCatalogName(),
                    statement.getSchemaName(), statement.getTableName());

            return generatedSqls;
        } catch (DatabaseException e) {
            throw new UnexpectedLiquibaseException(e);
        } catch (InvalidExampleException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }
}
