package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.database.ext.HanaDBDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.logging.LogFactory;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.AutoIncrementConstraint;
import liquibase.statement.ForeignKeyConstraint;
import liquibase.statement.UniqueConstraint;
import liquibase.statement.core.CreateTableStatement;
import liquibase.util.StringUtils;

import java.util.Iterator;

public class CreateTableGenerator extends AbstractSqlGenerator<CreateTableStatement> {

    public ValidationErrors validate(CreateTableStatement createTableStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", createTableStatement.getTableName());
        validationErrors.checkRequiredField("columns", createTableStatement.getColumns());
        return validationErrors;
    }

    public Sql[] generateSql(CreateTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
            StringBuffer buffer = new StringBuffer();
        buffer.append("CREATE TABLE ").append(database.escapeTableName(statement.getSchemaName(), statement.getTableName())).append(" ");
        buffer.append("(");
        
        boolean isSinglePrimaryKeyColumn = statement.getPrimaryKeyConstraint() != null
            && statement.getPrimaryKeyConstraint().getColumns().size() == 1;
        
        boolean isPrimaryKeyAutoIncrement = false;
        
        Iterator<String> columnIterator = statement.getColumns().iterator();
        while (columnIterator.hasNext()) {
            String column = columnIterator.next();
            
            buffer.append(database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), column));
            buffer.append(" ").append(statement.getColumnTypes().get(column));
            
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
                buffer.append(" PRIMARY KEY AUTOINCREMENT");
			}

            if (statement.getDefaultValue(column) != null) {
                Object defaultValue = statement.getDefaultValue(column);
                if (database instanceof MSSQLDatabase) {
                    buffer.append(" CONSTRAINT ").append(((MSSQLDatabase) database).generateDefaultConstraintName(statement.getTableName(), column));
                }
                buffer.append(" DEFAULT ");
                buffer.append(statement.getColumnTypes().get(column).convertObjectToString(defaultValue, database));
            }

            if (isAutoIncrementColumn) {
            	// TODO: check if database supports auto increment on non primary key column
                if (database.supportsAutoIncrement()) {
                	String autoIncrementClause = database.getAutoIncrementClause(autoIncrementConstraint.getStartWith(), autoIncrementConstraint.getIncrementBy());
                
                	if (!"".equals(autoIncrementClause)) {
                		buffer.append(" ").append(autoIncrementClause);
                	}
                } else {
                    LogFactory.getLogger().warning(database.getTypeName()+" does not support autoincrement columns as request for "+(database.escapeTableName(statement.getSchemaName(), statement.getTableName())));
                }
            }

            if (statement.getNotNullColumns().contains(column)) {
                buffer.append(" NOT NULL");
            } else {
                if (database instanceof SybaseDatabase || database instanceof SybaseASADatabase || database instanceof MySQLDatabase) {
                    if (database instanceof MySQLDatabase && statement.getColumnTypes().get(column).getDataTypeName().equalsIgnoreCase("timestamp")) {
                        //don't append null
                    } else {
                        buffer.append(" NULL");
                    }

                }
            }

            if (database instanceof InformixDatabase && isSinglePrimaryKeyColumn) {
            	buffer.append(" PRIMARY KEY");
            }

            if (columnIterator.hasNext()) {
                buffer.append(", ");
            }
        }

        buffer.append(",");

        // TODO informixdb
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
                if (!(database instanceof InformixDatabase) && !(database instanceof HanaDBDatabase)) {
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
            if (!referencesString.contains(".") && database.getDefaultSchemaName() != null) {
                referencesString = database.getDefaultSchemaName()+"."+referencesString;
            }
            buffer.append(" FOREIGN KEY (")
                    .append(database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), fkConstraint.getColumn()))
                    .append(") REFERENCES ")
                    .append(referencesString);

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
            if (uniqueConstraint.getConstraintName() != null && !constraintNameAfterUnique(database)) {
                buffer.append(" CONSTRAINT ");
                buffer.append(database.escapeConstraintName(uniqueConstraint.getConstraintName()));
            }
            buffer.append(" UNIQUE (");
            buffer.append(database.escapeColumnNameList(StringUtils.join(uniqueConstraint.getColumns(), ", ")));
            buffer.append(")");
            if (uniqueConstraint.getConstraintName() != null && constraintNameAfterUnique(database)) {
                buffer.append(" CONSTRAINT ");
                buffer.append(database.escapeConstraintName(uniqueConstraint.getConstraintName()));
            }
            buffer.append(",");
        }

//        if (constraints != null && constraints.getCheck() != null) {
//            buffer.append(constraints.getCheck()).append(" ");
//        }
//    }

        String sql = buffer.toString().replaceFirst(",\\s*$", "") + ")";

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

        return new Sql[] {
                new UnparsedSql(sql)
        };
    }

    private boolean constraintNameAfterUnique(Database database) {
		return database instanceof InformixDatabase;
	}

}
