package liquibase.sqlgenerator.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.Db2zDatabase;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.Firebird3Database;
import liquibase.database.core.FirebirdDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.ReorganizeTableStatement;
import liquibase.statement.core.SetNullableStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

public class SetNullableGenerator extends AbstractSqlGenerator<SetNullableStatement> {

    @Override
    public boolean supports(SetNullableStatement statement, Database database) {
        try {
            if (database instanceof Db2zDatabase || (database instanceof DB2Database) && (database.getDatabaseMajorVersion() > 0 && database.getDatabaseMajorVersion() < 9)) {
                //"DB2 versions less than 9 or z/OS do not support modifying null constraints";
                return false;
            }
        }
        catch (DatabaseException ignore) {
            //cannot check
        }

        return !(database instanceof SQLiteDatabase);
    }

    @Override
    public ValidationErrors validate(SetNullableStatement setNullableStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("tableName", setNullableStatement.getTableName());
        validationErrors.checkRequiredField("columnName", setNullableStatement.getColumnName());

        if ((database instanceof MSSQLDatabase) || (database instanceof MySQLDatabase) || (database instanceof InformixDatabase)) {
            validationErrors.checkRequiredField("columnDataType", setNullableStatement.getColumnDataType());
        }
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(SetNullableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String sql;

        String nullableString = statement.isNullable() ? " NULL" : " NOT NULL";

        if ((database instanceof OracleDatabase) && (statement.getConstraintName() != null)) {
            nullableString += !statement.isValidate() ? " ENABLE NOVALIDATE " : "";
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " MODIFY " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " CONSTRAINT " + statement.getConstraintName() + nullableString;
        }
        else if ((database instanceof OracleDatabase) || (database instanceof SybaseDatabase) || (database instanceof SybaseASADatabase)) {
            nullableString += (database instanceof OracleDatabase) && (!statement.isValidate()) ? " ENABLE NOVALIDATE " : "";
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " MODIFY " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + nullableString;
        }
        else if (database instanceof MSSQLDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + DataTypeFactory.getInstance().fromDescription(statement.getColumnDataType(),
                    database).toDatabaseDataType(database) + nullableString;
        }
        else if (database instanceof MySQLDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " MODIFY " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + DataTypeFactory.getInstance().fromDescription(statement.getColumnDataType(),
                    database).toDatabaseDataType(database) + nullableString;
        }
        else if (database instanceof DerbyDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN  " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + nullableString;
        }
        else if ((database instanceof HsqlDatabase) || (database instanceof H2Database)) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " SET" + nullableString;
        }
        else if (database instanceof InformixDatabase) {
            // Informix simply omits the null for nullables
            if (statement.isNullable()) {
                nullableString = "";
            }
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " MODIFY (" + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + DataTypeFactory.getInstance().fromDescription(statement.getColumnDataType(),
                    database).toDatabaseDataType(database) + nullableString + ")";
        }
        else if (database instanceof FirebirdDatabase && !(database instanceof Firebird3Database)) {
            // For Firebird database prior to Firebird 3 the ALTER TABLE syntax is not working
            // As a workaround we can modify the system table entry directly (see http://www.firebirdfaq.org/faq103/)
            sql = "UPDATE RDB$RELATION_FIELDS SET RDB$NULL_FLAG = " + (statement.isNullable() ? "NULL" : "1") + " WHERE RDB$RELATION_NAME = '" + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + "' AND RDB$FIELD_NAME = '" + database.escapeColumnName(statement.getCatalogName(),
                    statement.getSchemaName(),
                    statement.getTableName(),
                    statement.getColumnName()) + "'";
        }
        else {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + (statement.isNullable() ? " DROP NOT NULL" : " SET NOT NULL");
        }

        List<Sql> returnList = new ArrayList<>();
        returnList.add(new UnparsedSql(sql, getAffectedColumn(statement)));

        if (database instanceof DB2Database) {
            Sql[] a = SqlGeneratorFactory.getInstance().generateSql(new ReorganizeTableStatement(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()), database);
            if (a != null) {
                returnList.addAll(Arrays.asList(a));
            }
        }

        return returnList.toArray(new Sql[returnList.size()]);
    }

    protected Column getAffectedColumn(SetNullableStatement statement) {
        return new Column().setName(statement.getColumnName()).setRelation(new Table().setName(statement.getTableName()).setSchema(statement.getCatalogName(), statement.getSchemaName()));
    }
}
