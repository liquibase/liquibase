package liquibase.ext.bigquery.sqlgenerator;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.exception.DatabaseException;
import liquibase.ext.bigquery.database.BigqueryDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.CreateTableGenerator;
import liquibase.statement.*;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;
import liquibase.statement.core.CreateTableStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Sequence;
import liquibase.structure.core.Table;
import liquibase.util.StringUtil;

import java.math.BigInteger;
import java.util.*;

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
        System.out.println("Wlazlem do .... CreateTableGenerator");
        buffer.append("CREATE TABLE ").append(database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())).append(" ");
        buffer.append("(");
        boolean isSinglePrimaryKeyColumn = statement.getPrimaryKeyConstraint() != null && statement.getPrimaryKeyConstraint().getColumns().size() == 1;
        boolean isPrimaryKeyAutoIncrement = false;
        Iterator<String> columnIterator = statement.getColumns().iterator();
        BigInteger mysqlTableOptionStartWith = null;

        //List<String> cs = new ArrayList();
        //cs.add("name");
        //cs.add("id");

        //Iterator<String> columnIterator = cs.iterator();
        String column;
        while(columnIterator.hasNext()) {
                column = (String)columnIterator.next();
                DatabaseDataType columnType = null;
                if (statement.getColumnTypes().get(column) != null) {
                    columnType = ((LiquibaseDataType)statement.getColumnTypes().get(column)).toDatabaseDataType(database);
                }

                if (columnType == null) {
                    buffer.append(database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), column, false));
                } else {
                    buffer.append(database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), column, !statement.isComputed(column)));
                    buffer.append(" ").append(columnType);
                }

                AutoIncrementConstraint autoIncrementConstraint = null;
                Iterator var13 = statement.getAutoIncrementConstraints().iterator();

                while(var13.hasNext()) {
                    AutoIncrementConstraint currentAutoIncrementConstraint = (AutoIncrementConstraint)var13.next();
                    if (column.equals(currentAutoIncrementConstraint.getColumnName())) {
                        autoIncrementConstraint = currentAutoIncrementConstraint;
                        break;
                    }
                }

                boolean isAutoIncrementColumn = autoIncrementConstraint != null;
                boolean isPrimaryKeyColumn = statement.getPrimaryKeyConstraint() != null && statement.getPrimaryKeyConstraint().getColumns().contains(column);
                isPrimaryKeyAutoIncrement = isPrimaryKeyAutoIncrement || isPrimaryKeyColumn && isAutoIncrementColumn;
                String autoIncrementClause;
                if (database instanceof SQLiteDatabase && isSinglePrimaryKeyColumn && isPrimaryKeyColumn && isAutoIncrementColumn) {
                    autoIncrementClause = StringUtil.trimToNull(statement.getPrimaryKeyConstraint().getConstraintName());
                    if (autoIncrementClause == null) {
                        autoIncrementClause = database.generatePrimaryKeyName(statement.getTableName());
                    }

                    if (autoIncrementClause != null) {
                        buffer.append(" CONSTRAINT ");
                        buffer.append(database.escapeConstraintName(autoIncrementClause));
                    }

                    buffer.append(" PRIMARY KEY");
                }

                String nncName;
                if (columnType != null && !columnType.isAutoIncrement() && statement.getDefaultValue(column) != null) {
                    Object defaultValue = statement.getDefaultValue(column);
                    if (database instanceof MSSQLDatabase) {
                        nncName = statement.getDefaultValueConstraintName(column);
                        if (nncName == null) {
                            nncName = ((MSSQLDatabase)database).generateDefaultConstraintName(statement.getTableName(), column);
                        }

                        buffer.append(" CONSTRAINT ").append(database.escapeObjectName(nncName, ForeignKey.class));
                    }

                    if ((database instanceof OracleDatabase || database instanceof PostgresDatabase) && statement.getDefaultValue(column).toString().startsWith("GENERATED ALWAYS ")) {
                        buffer.append(" ");
                    } else if ((!(database instanceof Db2zDatabase) || !statement.getDefaultValue(column).toString().contains("CURRENT TIMESTAMP")) && !statement.getDefaultValue(column).toString().contains("IDENTITY GENERATED BY DEFAULT")) {
                        buffer.append(" DEFAULT ");
                    } else {
                        buffer.append(" ");
                    }

                    if (defaultValue instanceof DatabaseFunction) {
                        buffer.append(database.generateDatabaseFunctionValue((DatabaseFunction)defaultValue));
                    } else if (database instanceof Db2zDatabase) {
                        if (statement.getDefaultValue(column).toString().contains("CURRENT TIMESTAMP")) {
                            buffer.append("");
                        }

                        if (statement.getDefaultValue(column).toString().contains("IDENTITY GENERATED BY DEFAULT")) {
                            buffer.append("GENERATED BY DEFAULT AS IDENTITY");
                        }

                        if (statement.getDefaultValue(column).toString().contains("CURRENT USER")) {
                            buffer.append("SESSION_USER ");
                        }

                        if (statement.getDefaultValue(column).toString().contains("CURRENT SQLID")) {
                            buffer.append("CURRENT SQLID ");
                        }
                    } else {
                        buffer.append(((LiquibaseDataType)statement.getColumnTypes().get(column)).objectToSql(defaultValue, database));
                    }
                }

                if (isAutoIncrementColumn && (!(database instanceof PostgresDatabase) || !buffer.toString().toLowerCase().endsWith("serial"))) {
                    if (database.supportsAutoIncrement()) {
                        autoIncrementClause = database.getAutoIncrementClause(autoIncrementConstraint.getStartWith(), autoIncrementConstraint.getIncrementBy(), autoIncrementConstraint.getGenerationType(), autoIncrementConstraint.getDefaultOnNull());
                        if (!"".equals(autoIncrementClause)) {
                            buffer.append(" ").append(autoIncrementClause);
                        }

                        if (autoIncrementConstraint.getStartWith() != null) {
                            if (database instanceof PostgresDatabase) {
                                int majorVersion = 9;

                                try {
                                    majorVersion = database.getDatabaseMajorVersion();
                                } catch (DatabaseException var18) {
                                }

                                if (majorVersion < 10) {
                                    String sequenceName = statement.getTableName() + "_" + column + "_seq";
                                    additionalSql.add(new UnparsedSql("alter sequence " + database.escapeSequenceName(statement.getCatalogName(), statement.getSchemaName(), sequenceName) + " start with " + autoIncrementConstraint.getStartWith(), new DatabaseObject[]{(new Sequence()).setName(sequenceName).setSchema(statement.getCatalogName(), statement.getSchemaName())}));
                                }
                            } else if (database instanceof MySQLDatabase) {
                                mysqlTableOptionStartWith = autoIncrementConstraint.getStartWith();
                            }
                        }
                    } else {
                        Scope.getCurrentScope().getLog(this.getClass()).warning(database.getShortName() + " does not support autoincrement columns as requested for " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()));
                    }
                }

                if (statement.getNotNullColumns().get(column) != null) {
                    if (!database.supportsNotNullConstraintNames()) {
                        buffer.append(" NOT NULL");
                    } else {
                        NotNullConstraint nnConstraintForThisColumn = (NotNullConstraint)statement.getNotNullColumns().get(column);
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
                } else if (columnType != null && (database instanceof SybaseDatabase || database instanceof SybaseASADatabase || database instanceof MySQLDatabase || database instanceof MSSQLDatabase && columnType.toString().toLowerCase().contains("timestamp"))) {
                    buffer.append(" NULL");
                }

                if (database instanceof MySQLDatabase && statement.getColumnRemarks(column) != null) {
                    buffer.append(" COMMENT '" + database.escapeStringForDatabase(statement.getColumnRemarks(column)) + "'");
                }

                if (columnIterator.hasNext()) {
                    buffer.append(", ");
                }
            }


        buffer.append(",");
        if ((!(database instanceof SQLiteDatabase) || !isSinglePrimaryKeyColumn || !isPrimaryKeyAutoIncrement) && statement.getPrimaryKeyConstraint() != null && !statement.getPrimaryKeyConstraint().getColumns().isEmpty()) {
            if (database.supportsPrimaryKeyNames()) {
                column = StringUtil.trimToNull(statement.getPrimaryKeyConstraint().getConstraintName());
                if (column == null) {
                    column = database.generatePrimaryKeyName(statement.getTableName());
                }

                if (column != null) {
                    buffer.append(" CONSTRAINT ");
                    buffer.append(database.escapeConstraintName(column));
                }
            }

            buffer.append(" PRIMARY KEY (");
            buffer.append(database.escapeColumnNameList(StringUtil.join(statement.getPrimaryKeyConstraint().getColumns(), ", ")));
            buffer.append(")");
            if ((database instanceof OracleDatabase || database instanceof PostgresDatabase) && statement.getPrimaryKeyConstraint().getTablespace() != null) {
                buffer.append(" USING INDEX TABLESPACE ");
                buffer.append(statement.getPrimaryKeyConstraint().getTablespace());
            }

            buffer.append(!statement.getPrimaryKeyConstraint().shouldValidatePrimaryKey() ? " ENABLE NOVALIDATE " : "");
            if (database.supportsInitiallyDeferrableColumns()) {
                if (statement.getPrimaryKeyConstraint().isInitiallyDeferred()) {
                    buffer.append(" INITIALLY DEFERRED");
                }

                if (statement.getPrimaryKeyConstraint().isDeferrable()) {
                    buffer.append(" DEFERRABLE");
                }
            }

            buffer.append(",");
        }

        String sql;
        for(Iterator var19 = statement.getForeignKeyConstraints().iterator(); var19.hasNext(); buffer.append(",")) {
            ForeignKeyConstraint fkConstraint = (ForeignKeyConstraint)var19.next();
            if (!(database instanceof InformixDatabase)) {
                buffer.append(" CONSTRAINT ");
                buffer.append(database.escapeConstraintName(fkConstraint.getForeignKeyName()));
            }

            sql = fkConstraint.getReferences();
            buffer.append(" FOREIGN KEY (").append(database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), fkConstraint.getColumn())).append(") REFERENCES ");
            if (sql != null) {
                if (!sql.contains(".") && database.getDefaultSchemaName() != null && database.getOutputDefaultSchema()) {
                    sql = database.escapeObjectName(database.getDefaultSchemaName(), Schema.class) + "." + sql;
                }

                buffer.append(sql);
            } else {
                buffer.append(database.escapeObjectName(fkConstraint.getReferencedTableCatalogName(), fkConstraint.getReferencedTableSchemaName(), fkConstraint.getReferencedTableName(), Table.class)).append("(").append(database.escapeColumnNameList(fkConstraint.getReferencedColumnNames())).append(")");
            }

            if (fkConstraint.isDeleteCascade()) {
                buffer.append(" ON DELETE CASCADE");
            }

            if (database instanceof InformixDatabase) {
                buffer.append(" CONSTRAINT ");
                buffer.append(database.escapeConstraintName(fkConstraint.getForeignKeyName()));
            }

            if (fkConstraint.isInitiallyDeferred()) {
                buffer.append(" INITIALLY DEFERRED");
            }

            if (fkConstraint.isDeferrable()) {
                buffer.append(" DEFERRABLE");
            }

            if (database instanceof OracleDatabase) {
                buffer.append(!fkConstraint.shouldValidateForeignKey() ? " ENABLE NOVALIDATE " : "");
            }
        }

        LinkedHashMap<String, UniqueConstraint> namedUniqueConstraints = new LinkedHashMap();
        List<UniqueConstraint> unnamedUniqueConstraints = new LinkedList();
        Iterator var26 = statement.getUniqueConstraints().iterator();

        UniqueConstraint uniqueConstraint;
        while(var26.hasNext()) {
            uniqueConstraint = (UniqueConstraint)var26.next();
            if (uniqueConstraint.getConstraintName() == null) {
                unnamedUniqueConstraints.add(uniqueConstraint);
            } else {
                String constraintName = uniqueConstraint.getConstraintName();
                UniqueConstraint existingConstraint = (UniqueConstraint)namedUniqueConstraints.get(constraintName);
                if (existingConstraint != null) {
                    if (uniqueConstraint.shouldValidateUnique()) {
                        existingConstraint.setValidateUnique(true);
                    }

                    existingConstraint.getColumns().addAll(uniqueConstraint.getColumns());
                } else {
                    namedUniqueConstraints.put(constraintName, uniqueConstraint);
                }
            }
        }

        unnamedUniqueConstraints.addAll(namedUniqueConstraints.values());

        for(var26 = unnamedUniqueConstraints.iterator(); var26.hasNext(); buffer.append(",")) {
            uniqueConstraint = (UniqueConstraint)var26.next();
            if (uniqueConstraint.getConstraintName() != null) {
                buffer.append(" CONSTRAINT ");
                buffer.append(database.escapeConstraintName(uniqueConstraint.getConstraintName()));
            }

            buffer.append(" UNIQUE (");
            buffer.append(database.escapeColumnNameList(StringUtil.join(uniqueConstraint.getColumns(), ", ")));
            buffer.append(")");
            if (database instanceof OracleDatabase) {
                buffer.append(!uniqueConstraint.shouldValidateUnique() ? " ENABLE NOVALIDATE " : "");
            }
        }

        sql = buffer.toString().replaceFirst(",\\s*$", "") + ")";
        if (database instanceof MySQLDatabase && mysqlTableOptionStartWith != null) {
            Scope.getCurrentScope().getLog(this.getClass()).info("[MySQL] Using last startWith statement (" + mysqlTableOptionStartWith.toString() + ") as table option.");
            sql = sql + " " + ((MySQLDatabase)database).getTableOptionAutoIncrementStartWithClause(mysqlTableOptionStartWith);
        }

        if (statement.getTablespace() != null && database.supportsTablespaces()) {
            if (!(database instanceof MSSQLDatabase) && !(database instanceof SybaseASADatabase)) {
                if (!(database instanceof AbstractDb2Database) && !(database instanceof InformixDatabase)) {
                    sql = sql + " TABLESPACE " + statement.getTablespace();
                } else {
                    sql = sql + " IN " + statement.getTablespace();
                }
            } else {
                sql = sql + " ON " + statement.getTablespace();
            }
        }

        if (database instanceof MySQLDatabase && statement.getRemarks() != null) {
            sql = sql + " COMMENT='" + database.escapeStringForDatabase(statement.getRemarks()) + "' ";
        }

        additionalSql.add(0, new UnparsedSql(sql, new DatabaseObject[]{this.getAffectedTable(statement)}));
        return (Sql[])additionalSql.toArray(new Sql[additionalSql.size()]);
    }
}
