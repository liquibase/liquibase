package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DatabaseDataType;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.logging.LogFactory;
import liquibase.statement.AutoIncrementConstraint;
import liquibase.statement.ForeignKeyConstraint;
import liquibase.statement.UniqueConstraint;
import liquibase.statement.core.CreateTableStatement;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CreateTableGenerator extends AbstractSqlGenerator<CreateTableStatement> {

    @Override
    public ValidationErrors validate(CreateTableStatement createTableStatement, ExecutionEnvironment env, StatementLogicChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", createTableStatement.getTableName());
        validationErrors.checkRequiredField("columns", createTableStatement.getColumnNames());
        return validationErrors;
    }

    @Override
    public Action[] generateActions(CreateTableStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        Database database = env.getTargetDatabase();

        if (database instanceof InformixDatabase) {
            AbstractSqlGenerator<CreateTableStatement> gen = new CreateTableGeneratorInformix();
    		return gen.generateActions(statement, env, chain);
    	}

        List<Action> additionalSql = new ArrayList<Action>();
    	
        StringBuffer buffer = new StringBuffer();
        buffer.append("CREATE TABLE ").append(database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())).append(" ");
        buffer.append("(");
        
        boolean isSinglePrimaryKeyColumn = statement.getPrimaryKeyConstraint() != null
            && statement.getPrimaryKeyConstraint().getColumns().size() == 1;
        
        boolean isPrimaryKeyAutoIncrement = false;
        
        Iterator<String> columnIterator = statement.getColumnNames().iterator();
        List<String> primaryKeyColumns = new LinkedList<String>();

        BigInteger mysqlTableOptionStartWith = null;

        while (columnIterator.hasNext()) {
            String column = columnIterator.next();
            DatabaseDataType columnType = statement.getColumnType(column).toDatabaseDataType(database);
            buffer.append(database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), column));

            buffer.append(" ").append(columnType);

            AutoIncrementConstraint autoIncrementConstraint = null;
            
            for (AutoIncrementConstraint currentAutoIncrementConstraint : statement.getAutoIncrementConstraints()) {
                if (column.equals(currentAutoIncrementConstraint.getColumnName())) {
                    autoIncrementConstraint = currentAutoIncrementConstraint;
                    break;
                }
            }

            boolean isAutoIncrementColumn = autoIncrementConstraint != null;
            boolean isPrimaryKeyColumn = statement.getPrimaryKeyConstraint() != null
                    && statement.getPrimaryKeyConstraint().getColumns().contains(column);
            isPrimaryKeyAutoIncrement = isPrimaryKeyAutoIncrement
                    || isPrimaryKeyColumn && isAutoIncrementColumn;
            
            if (isPrimaryKeyColumn) {
            	primaryKeyColumns.add(column);
            }
            
            if ((database instanceof SQLiteDatabase) &&
                    isSinglePrimaryKeyColumn &&
                    isPrimaryKeyColumn &&
                    isAutoIncrementColumn) {
                String pkName = StringUtils.trimToNull(statement.getPrimaryKeyConstraint().getConstraintName());
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
            if (!columnType.isAutoIncrement() && statement.getDefaultValue(column) != null) {
                Object defaultValue = statement.getDefaultValue(column);
                if (database instanceof MSSQLDatabase) {
                    buffer.append(" CONSTRAINT ").append(((MSSQLDatabase) database).generateDefaultConstraintName(statement.getTableName(), column));
                }
                buffer.append(" DEFAULT ");
                buffer.append(statement.getColumnType(column).objectToSql(defaultValue, database));
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
	                        additionalSql.add(new UnparsedSql("alter sequence "+ database.escapeSequenceName(statement.getCatalogName(), statement.getSchemaName(), sequenceName)+" start with "+autoIncrementConstraint.getStartWith()));
	                    }else if(database instanceof MySQLDatabase){
	                    	mysqlTableOptionStartWith = autoIncrementConstraint.getStartWith();
	                    }
                    }
                } else {
                    LogFactory.getLogger().warning(database.getShortName()+" does not support autoincrement columns as requested for "+(database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())));
                }
            }

            if (statement.getNotNullConstraint(column) != null) {
                buffer.append(" NOT NULL");
            } else {
                if (database instanceof SybaseDatabase || database instanceof SybaseASADatabase || database instanceof MySQLDatabase) {
                    buffer.append(" NULL");
                }
            }

            if (database instanceof InformixDatabase && isSinglePrimaryKeyColumn && isPrimaryKeyColumn) {
                //buffer.append(" PRIMARY KEY");
            }

            if(database instanceof MySQLDatabase && statement.getColumnRemarks(column) != null){
                buffer.append(" COMMENT '" + database.escapeStringForDatabase(statement.getColumnRemarks(column)) + "'");

            }

            if (columnIterator.hasNext()) {
                buffer.append(", ");
            }
        }

        buffer.append(",");

        if (!( (database instanceof SQLiteDatabase) &&
                isSinglePrimaryKeyColumn &&
                isPrimaryKeyAutoIncrement) &&

                !((database instanceof InformixDatabase) &&
                isSinglePrimaryKeyColumn
                )) {
            // ...skip this code block for sqlite if a single column primary key
            // with an autoincrement constraint exists.
            // This constraint is added after the column type.

            if (statement.getPrimaryKeyConstraint() != null && statement.getPrimaryKeyConstraint().getColumns().size() > 0) {
                if (database.supportsPrimaryKeyNames()) {
                    String pkName = StringUtils.trimToNull(statement.getPrimaryKeyConstraint().getConstraintName());
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
                buffer.append(database.escapeColumnNameList(StringUtils.join(statement.getPrimaryKeyConstraint().getColumns(), ", ")));
                buffer.append(")");
                // Setting up table space for PK's index if it exist
                if (database instanceof OracleDatabase &&
                    statement.getPrimaryKeyConstraint().getTablespace() != null) {
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
                    .append(StringUtils.join(fkConstraint.getColumnNames(), ", "))
                    .append(") REFERENCES ");
            if (referencesString != null) {
                if (!referencesString.contains(".") && database.getDefaultSchemaName() != null && database.getOutputDefaultSchema()) {
                    referencesString = database.getDefaultSchemaName() +"."+referencesString;
                }
                buffer.append(referencesString);
            } else {
                buffer.append(database.escapeObjectName(fkConstraint.getReferencedTableName(), Table.class))
                    .append("(")
                    .append(database.escapeColumnNameList(fkConstraint.getReferencedColumnNames()))
                    .append(")");

            }


            if (fkConstraint.getDeleteCascade()) {
                buffer.append(" ON DELETE CASCADE");
            }

            if ((database instanceof InformixDatabase)) {
                buffer.append(" CONSTRAINT ");
                buffer.append(database.escapeConstraintName(fkConstraint.getForeignKeyName()));
            }

            if (fkConstraint.getInitiallyDeferred()) {
                buffer.append(" INITIALLY DEFERRED");
            }
            if (fkConstraint.getDeferrable()) {
                buffer.append(" DEFERRABLE");
            }
            buffer.append(",");
        }

        for (UniqueConstraint uniqueConstraint : statement.getUniqueConstraints()) {
            if (uniqueConstraint.getConstraintName() != null && !constraintNameAfterUnique(database)) {
                buffer.append(" CONSTRAINT ");
                buffer.append(database.escapeConstraintName(uniqueConstraint.getConstraintName()));
            }
            buffer.append(" UNIQUE (");
            buffer.append(database.escapeColumnNameList(StringUtils.join(uniqueConstraint.getColumnNames(), ", ")));
            buffer.append(")");
            if (uniqueConstraint.getConstraintName() != null && constraintNameAfterUnique(database)) {
                buffer.append(" CONSTRAINT ");
                buffer.append(database.escapeConstraintName(uniqueConstraint.getConstraintName()));
            }
            buffer.append(",");
        }

//        if (constraints != null && constraints.getCheckConstraint() != null) {
//            buffer.append(constraints.getCheckConstraint()).append(" ");
//        }
//    }


        String sql = buffer.toString().replaceFirst(",\\s*$", "")+")";

        if (database instanceof MySQLDatabase && mysqlTableOptionStartWith != null){
        	LogFactory.getLogger().info("[MySQL] Using last startWith statement ("+mysqlTableOptionStartWith.toString()+") as table option.");
        	sql += " "+((MySQLDatabase) database).getTableOptionAutoIncrementStartWithClause(mysqlTableOptionStartWith);
        }


//        if (StringUtils.trimToNull(tablespace) != null && database.supportsTablespaces()) {
//            if (database instanceof MSSQLDatabase) {
//                buffer.append(" ON ").append(tablespace);
//            } else if (database instanceof DB2Database) {
//                buffer.append(" IN ").append(tablespace);
//            } else {
//                buffer.append(" TABLESPACE ").append(tablespace);
//            }
//        }

        if (statement.getTablespace() != null && database.supportsTablespaces()) {
            if (database instanceof MSSQLDatabase || database instanceof SybaseASADatabase) {
                sql += " ON " + statement.getTablespace();
            } else if (database instanceof DB2Database || database instanceof InformixDatabase) {
                sql += " IN " + statement.getTablespace();
            } else {
                sql += " TABLESPACE " + statement.getTablespace();
            }
        }

        if( database instanceof MySQLDatabase && statement.getRemarks() != null) {
            sql += " COMMENT='"+ database.escapeStringForDatabase(statement.getRemarks())+"' ";
        }
        additionalSql.add(0, new UnparsedSql(sql));
        return additionalSql.toArray(new Action[additionalSql.size()]);
    }

    private boolean constraintNameAfterUnique(Database database) {
        return database instanceof InformixDatabase;
    }

}
