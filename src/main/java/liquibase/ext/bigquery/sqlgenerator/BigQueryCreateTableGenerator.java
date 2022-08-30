package liquibase.ext.bigquery.sqlgenerator;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.CreateTableGenerator;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.core.CreateTableStatement;
import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static liquibase.ext.bigquery.database.BigqueryDatabase.BIGQUERY_PRIORITY_DATABASE;

public class BigQueryCreateTableGenerator extends CreateTableGenerator {

    @Override
    public int getPriority() {
        return BIGQUERY_PRIORITY_DATABASE;
    }

    @Override
    public Sql[] generateSql(CreateTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        List<Sql> additionalSql = new ArrayList();
        StringBuilder buffer = new StringBuilder();
        Scope.getCurrentScope().getLog(this.getClass()).info("inside CreateTableGenerator");
        buffer.append("CREATE TABLE ").append(database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())).append(" ");
        buffer.append("(");
        Iterator<String> columnIterator = statement.getColumns().iterator();

        String column;
        while (columnIterator.hasNext()) {
            column = (String) columnIterator.next();
            DatabaseDataType columnType = null;
            if (statement.getColumnTypes().get(column) != null) {
                columnType = ((LiquibaseDataType) statement.getColumnTypes().get(column)).toDatabaseDataType(database);
            }

            if (columnType == null) {
                buffer.append(database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), column, false));
            } else {
                buffer.append(database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), column, !statement.isComputed(column)));
                buffer.append(" ").append(columnType);
            }

            String nncName;
            if (columnType != null && !columnType.isAutoIncrement() && statement.getDefaultValue(column) != null) {
                Object defaultValue = statement.getDefaultValue(column);

                if (defaultValue instanceof DatabaseFunction) {
                    buffer.append(database.generateDatabaseFunctionValue((DatabaseFunction) defaultValue));
                } else {
                    buffer.append(((LiquibaseDataType) statement.getColumnTypes().get(column)).objectToSql(defaultValue, database));
                }
            }

            if (statement.getNotNullColumns().get(column) != null) {
                if (!database.supportsNotNullConstraintNames()) {
                    buffer.append(" NOT NULL");
                } else {
                    NotNullConstraint nnConstraintForThisColumn = (NotNullConstraint) statement.getNotNullColumns().get(column);
                    nncName = StringUtil.trimToNull(nnConstraintForThisColumn.getConstraintName());
                    if (nncName == null) {
                        buffer.append(" NOT NULL");
                    } else {
                        buffer.append(" CONSTRAINT ");
                        buffer.append(database.escapeConstraintName(nncName));
                        buffer.append(" NOT NULL");
                    }

                    if (!nnConstraintForThisColumn.shouldValidateNullable() && database instanceof OracleDatabase) {
                        buffer.append(" ENABLE NOVALIDATE ");
                    }
                }
            }
            if (columnIterator.hasNext()) {
                buffer.append(", ");
            }
        }


        buffer.append(",");
        String sql = buffer.toString().replaceFirst(",\\s*$", "") + ")";

        additionalSql.add(0, new UnparsedSql(sql, new DatabaseObject[]{this.getAffectedTable(statement)}));
        return (Sql[]) additionalSql.toArray(new Sql[additionalSql.size()]);
    }
}
