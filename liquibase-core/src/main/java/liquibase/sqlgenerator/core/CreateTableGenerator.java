package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DatabaseDataType;
import liquibase.exception.ValidationErrors;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.*;
import liquibase.statement.UniqueConstraint;
import liquibase.statement.core.CreateTableStatement;
import liquibase.structure.core.*;
import liquibase.util.StringUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CreateTableGenerator extends AbstractSqlGenerator<CreateTableStatement> {

    @Override
    public ValidationErrors validate(CreateTableStatement createTableStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", createTableStatement.getTableName());
        validationErrors.checkRequiredField("columns", createTableStatement.getColumns());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(CreateTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {

        List<Sql> additionalSql = new ArrayList<>();

        StringBuffer buffer = new StringBuffer();
        buffer.append("CREATE TABLE ").append(database.escapeTableName(statement.getCatalogName(),
            statement.getSchemaName(), statement.getTableName())).append(" ");
        buffer.append("(");
        
        boolean isSinglePrimaryKeyColumn = (statement.getPrimaryKeyConstraint() != null) && (statement
            .getPrimaryKeyConstraint().getColumns().size() == 1);
        
        boolean isPrimaryKeyAutoIncrement = false;
        
        Iterator<String> columnIterator = statement.getColumns().iterator();

        BigInteger mysqlTableOptionStartWith = null;

        /* We have reached the point after "CREATE TABLE ... (" and will now iterate through the column list. */
        while (columnIterator.hasNext()) {
            String column = columnIterator.next();
            DatabaseDataType columnType = statement.getColumnTypes().get(column).toDatabaseDataType(database);
            buffer.append(database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), column, true));

            buffer.append(" ").append(columnType);

            AutoIncrementConstraint autoIncrementConstraint = null;
            
            for (AutoIncrementConstraint currentAutoIncrementConstraint : statement.getAutoIncrementConstraints()) {
                if (column.equals(currentAutoIncrementConstraint.getColumnName())) {
                    autoIncrementConstraint = currentAutoIncrementConstraint;
                    break;
                }
            }

            boolean isAutoIncrementColumn = autoIncrementConstraint != null;
            boolean isPrimaryKeyColumn = (statement.getPrimaryKeyConstraint() != null) && statement
                .getPrimaryKeyConstraint().getColumns().contains(column);
            isPrimaryKeyAutoIncrement = isPrimaryKeyAutoIncrement || (isPrimaryKeyColumn && isAutoIncrementColumn);

            if ((database instanceof SQLiteDatabase) &&
                    isSinglePrimaryKeyColumn &&
                    isPrimaryKeyColumn &&
                    isAutoIncrementColumn) {
                String pkName = StringUtil.trimToNull(statement.getPrimaryKeyConstraint().getConstraintName());
                if (pkName == null) {
                    pkName = database.generatePrimaryKeyName(statement.getTableName());
                }
                if (pkName != null) {
                    buffer.append(" CONSTRAINT ");
                    buffer.append(database.escapeConstraintName(pkName));
                }
                buffer.append(" PRIMARY KEY");
            }

            // for the serial data type in postgres, there should be no default value
            if (!columnType.isAutoIncrement() && (statement.getDefaultValue(column) != null)) {
                Object defaultValue = statement.getDefaultValue(column);
                if (database instanceof MSSQLDatabase) {
                    String constraintName = statement.getDefaultValueConstraintName(column);
                    if (constraintName == null) {
                        constraintName = ((MSSQLDatabase) database).generateDefaultConstraintName(statement.getTableName(), column);
                    }
                    buffer.append(" CONSTRAINT ").append(database.escapeObjectName(constraintName, ForeignKey.class));
                }
                if ((database instanceof OracleDatabase) && statement.getDefaultValue(column).toString().startsWith
                    ("GENERATED ALWAYS ")) {
                    buffer.append(" ");
                } else if (database instanceof Db2zDatabase && statement.getDefaultValue(column).toString().contains("CURRENT TIMESTAMP")
                        || statement.getDefaultValue(column).toString().contains("IDENTITY GENERATED BY DEFAULT")) {
                    buffer.append(" ");
                } else {
                    buffer.append(" DEFAULT ");
                }

                if (defaultValue instanceof SequenceNextValueFunction) {
                    buffer.append(database.generateDatabaseFunctionValue((SequenceNextValueFunction) defaultValue));
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
                    buffer.append(statement.getColumnTypes().get(column).objectToSql(defaultValue, database));
                }
            }

            if (isAutoIncrementColumn) {
                // TODO: check if database supports auto increment on non primary key column
                if (database.supportsAutoIncrement()) {
                    String autoIncrementClause = database.getAutoIncrementClause(autoIncrementConstraint.getStartWith(), autoIncrementConstraint.getIncrementBy());
                
                    if (!"".equals(autoIncrementClause)) {
                        buffer.append(" ").append(autoIncrementClause);
                    }

                    if( autoIncrementConstraint.getStartWith() != null ){
                        if (database instanceof PostgresDatabase) {
                            String sequenceName = statement.getTableName()+"_"+column+"_seq";
                            additionalSql.add(new UnparsedSql("alter sequence "+database.escapeSequenceName(statement.getCatalogName(), statement.getSchemaName(), sequenceName)+" start with "+autoIncrementConstraint.getStartWith(), new Sequence().setName(sequenceName).setSchema(statement.getCatalogName(), statement.getSchemaName())));
                        }else if(database instanceof MySQLDatabase){
                            mysqlTableOptionStartWith = autoIncrementConstraint.getStartWith();
                        }
                    }
                } else {
                    LogService.getLog(getClass()).warning(LogType.LOG, database.getShortName()+" does not support autoincrement columns as requested for "+(database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())));
                }
            }

            // Do we have a NOT NULL constraint for this column?
            if (statement.getNotNullColumns().get(column) != null) {
                if (! database.supportsNotNullConstraintNames()) {
                    buffer.append(" NOT NULL");
                } else {
                    /* Determine if the NOT NULL constraint has a name. */
                    NotNullConstraint nnConstraintForThisColumn = statement.getNotNullColumns().get(column);
                    String nncName = StringUtil.trimToNull(nnConstraintForThisColumn.getName());
                    if (nncName == null) {
                        buffer.append(" NOT NULL");
                    } else {
                        buffer.append(" CONSTRAINT ");
                        buffer.append(database.escapeConstraintName(nncName));
                        buffer.append(" NOT NULL");
                    } // do we have a NN constraint name?
                } // does the DB support constraint names?
            } else {
                if ((database instanceof SybaseDatabase) || (database instanceof SybaseASADatabase) || (database
                    instanceof MySQLDatabase) || ((database instanceof MSSQLDatabase) && columnType.toString()
                    .toLowerCase().contains("timestamp"))) {
                    buffer.append(" NULL");
                } // Do we need to specify NULL explicitly?
            } // Do we have a NOT NULL constraint for this column?

            if ((database instanceof MySQLDatabase) && (statement.getColumnRemarks(column) != null)) {
                buffer.append(" COMMENT '" + database.escapeStringForDatabase(statement.getColumnRemarks(column)) + "'");
            }

            if (columnIterator.hasNext()) {
                buffer.append(", ");
            }
        }

        buffer.append(",");

        if (!( (database instanceof SQLiteDatabase) &&
                isSinglePrimaryKeyColumn &&
                isPrimaryKeyAutoIncrement) ) {

            if ((statement.getPrimaryKeyConstraint() != null) && !statement.getPrimaryKeyConstraint().getColumns()
                .isEmpty()) {
                if (database.supportsPrimaryKeyNames()) {
                    String pkName = StringUtil.trimToNull(statement.getPrimaryKeyConstraint().getConstraintName());
                    if (pkName == null) {
                        // TODO ORA-00972: identifier is too long
                        // If tableName lenght is more then 28 symbols
                        // then generated pkName will be incorrect
                        pkName = database.generatePrimaryKeyName(statement.getTableName());
                    }
                    if (pkName != null) {
                        buffer.append(" CONSTRAINT ");
                        buffer.append(database.escapeConstraintName(pkName));
                    }
                }
                buffer.append(" PRIMARY KEY (");
                buffer.append(database.escapeColumnNameList(StringUtil.join(statement.getPrimaryKeyConstraint().getColumns(), ", ")));
                buffer.append(")");
                // Setting up table space for PK's index if it exist
                if (((database instanceof OracleDatabase) || (database instanceof PostgresDatabase)) && (statement
                    .getPrimaryKeyConstraint().getTablespace() != null)) {
                    buffer.append(" USING INDEX TABLESPACE ");
                    buffer.append(statement.getPrimaryKeyConstraint().getTablespace());
                }

                buffer.append(",");
            }
        }

        for (ForeignKeyConstraint fkConstraint : statement.getForeignKeyConstraints()) {
            if (!(database instanceof InformixDatabase)) {
                buffer.append(" CONSTRAINT ");
                buffer.append(database.escapeConstraintName(fkConstraint.getForeignKeyName()));
            }
            String referencesString = fkConstraint.getReferences();

            buffer.append(" FOREIGN KEY (")
                    .append(database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), fkConstraint.getColumn()))
                    .append(") REFERENCES ");
            if (referencesString != null) {
                if (!referencesString.contains(".") && (database.getDefaultSchemaName() != null) && database
                    .getOutputDefaultSchema()) {
                    referencesString = database.escapeObjectName(database.getDefaultSchemaName(), Schema.class) +"."+referencesString;
                }
                buffer.append(referencesString);
            } else {
                buffer.append(database.escapeObjectName(fkConstraint.getReferencedTableCatalogName(), fkConstraint.getReferencedTableSchemaName(), fkConstraint.getReferencedTableName(), Table.class))
                    .append("(")
                    .append(database.escapeColumnNameList(fkConstraint.getReferencedColumnNames()))
                    .append(")");

            }


            if (fkConstraint.isDeleteCascade()) {
                buffer.append(" ON DELETE CASCADE");
            }

            if ((database instanceof InformixDatabase)) {
                buffer.append(" CONSTRAINT ");
                buffer.append(database.escapeConstraintName(fkConstraint.getForeignKeyName()));
            }

            if (fkConstraint.isInitiallyDeferred()) {
                buffer.append(" INITIALLY DEFERRED");
            }
            if (fkConstraint.isDeferrable()) {
                buffer.append(" DEFERRABLE");
            }
            buffer.append(",");
        }

        for (UniqueConstraint uniqueConstraint : statement.getUniqueConstraints()) {
            if (uniqueConstraint.getConstraintName() != null) {
                buffer.append(" CONSTRAINT ");
                buffer.append(database.escapeConstraintName(uniqueConstraint.getConstraintName()));
            }
            buffer.append(" UNIQUE (");
            buffer.append(database.escapeColumnNameList(StringUtil.join(uniqueConstraint.getColumns(), ", ")));
            buffer.append(")");
            buffer.append(",");
        }

        /*
         * Here, the list of columns and constraints in the form
         * ( column1, ..., columnN, constraint1, ..., constraintN,
         * ends. We cannot leave an expression like ", )", so we remove the last comma.
         */
        String sql = buffer.toString().replaceFirst(",\\s*$", "")+")";

        if ((database instanceof MySQLDatabase) && (mysqlTableOptionStartWith != null)){
            LogService.getLog(getClass()).info(LogType.LOG, "[MySQL] Using last startWith statement ("+mysqlTableOptionStartWith.toString()+") as table option.");
            sql += " "+((MySQLDatabase)database).getTableOptionAutoIncrementStartWithClause(mysqlTableOptionStartWith);
        }

        if ((statement.getTablespace() != null) && database.supportsTablespaces()) {
            if ((database instanceof MSSQLDatabase) || (database instanceof SybaseASADatabase)) {
                sql += " ON " + statement.getTablespace();
            } else if ((database instanceof AbstractDb2Database) || (database instanceof InformixDatabase)) {
                sql += " IN " + statement.getTablespace();
            } else {
                sql += " TABLESPACE " + statement.getTablespace();
            }
        }

        if((database instanceof MySQLDatabase) && (statement.getRemarks() != null)) {
            sql += " COMMENT='"+database.escapeStringForDatabase(statement.getRemarks())+"' ";
        }
        additionalSql.add(0, new UnparsedSql(sql, getAffectedTable(statement)));
        return additionalSql.toArray(new Sql[additionalSql.size()]);
    }

    protected Relation getAffectedTable(CreateTableStatement statement) {
        return new Table().setName(statement.getTableName()).setSchema(new Schema(statement.getCatalogName(), statement.getSchemaName()));
    }

}
