package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddDefaultValueStatement;
import liquibase.structure.core.Column;

public class AddDefaultValueGeneratorMSSQL extends AddDefaultValueGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddDefaultValueStatement statement, Database database) {
        return database instanceof MSSQLDatabase;
    }

    @Override
    public Sql[] generateSql(AddDefaultValueStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        Object defaultValue = statement.getDefaultValue();
        String constraintName = statement.getDefaultValueConstraintName();
        if (constraintName == null) {
            constraintName = ((MSSQLDatabase) database).generateDefaultConstraintName(statement.getTableName(), statement.getColumnName());
        }
        return new Sql[] {
                new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ADD CONSTRAINT " + database.escapeObjectName(constraintName, Column.class) + " DEFAULT " + DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database) + " FOR " + database.escapeObjectName(statement.getColumnName(), Column.class),
                        getAffectedColumn(statement))
        };
    }
}