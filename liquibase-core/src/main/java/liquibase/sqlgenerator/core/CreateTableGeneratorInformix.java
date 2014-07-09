package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import  liquibase.ExecutionEnvironment;
import liquibase.logging.LogFactory;
import liquibase.statement.AutoIncrementConstraint;
import liquibase.statement.ForeignKeyConstraint;
import liquibase.statement.UniqueConstraint;
import liquibase.statement.core.CreateTableStatement;
import liquibase.util.StringUtils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * An Informix-specific create table statement generator.
 * 
 * @author islavov
 */
public class CreateTableGeneratorInformix extends CreateTableGenerator {

    @Override
    public boolean supports(CreateTableStatement statement, ExecutionEnvironment env) {
        return env.getTargetDatabase() instanceof InformixDatabase;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public Action[] generateActions(CreateTableStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        StringBuilder buffer = new StringBuilder();
        Database database = env.getTargetDatabase();


        buffer.append("CREATE TABLE ").append(database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())).append(" ");
        buffer.append("(");
        
        boolean isSinglePrimaryKeyColumn = 
        		statement.getPrimaryKeyConstraint() != null && 
        		statement.getPrimaryKeyConstraint().getColumns().size() == 1;
        
        boolean isPrimaryKeyAutoIncrement = false;
        
        Iterator<String> columnIterator = statement.getColumnNames().iterator();
        List<String> primaryKeyColumns = new LinkedList<String>();
        while (columnIterator.hasNext()) {
            String column = columnIterator.next();
            
            buffer.append(database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), column));
            buffer.append(" ").append(statement.getColumnType(column).toDatabaseDataType(database).toSql());
            
            AutoIncrementConstraint autoIncrementConstraint = null;
            
            for (AutoIncrementConstraint currentAutoIncrementConstraint : statement.getAutoIncrementConstraints()) {
                if (column.equals(currentAutoIncrementConstraint.getColumnName())) {
                    autoIncrementConstraint = currentAutoIncrementConstraint;
                    break;
                }
            }

            boolean isAutoIncrementColumn = autoIncrementConstraint != null;            
            boolean isPrimaryKeyColumn = statement.getPrimaryKeyConstraint() != null && 
            		statement.getPrimaryKeyConstraint().getColumns().contains(column);
            isPrimaryKeyAutoIncrement = isPrimaryKeyAutoIncrement || isPrimaryKeyColumn && isAutoIncrementColumn;
            
            if (isPrimaryKeyColumn) {
            	primaryKeyColumns.add(column);
            }
            
            if (statement.getDefaultValue(column) != null) {
            	Object defaultValue = statement.getDefaultValue(column);
                buffer.append(" DEFAULT ");
                buffer.append(statement.getColumnType(column).objectToSql(defaultValue, database));
            }

            if (isAutoIncrementColumn) {
                // TODO: check if database supports auto increment on non primary key column
                if (database.supportsAutoIncrement()) {
                    String autoIncrementClause = database.getAutoIncrementClause(autoIncrementConstraint.getStartWith(), autoIncrementConstraint.getIncrementBy());
                
                    if (autoIncrementClause.length() > 0) {
                        buffer.append(" ").append(autoIncrementClause);
                    }
                } else {
                    LogFactory.getLogger().warning(database.getShortName()+" does not support autoincrement columns as requested for "+(database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())));
                }
            }

            if (statement.getNotNullConstraint(column) != null) {
                buffer.append(" NOT NULL");
            }

            if (columnIterator.hasNext()) {
                buffer.append(", ");
            }
        }

        buffer.append(",");

        // TODO informixdb
        if (!( (isSinglePrimaryKeyColumn && isPrimaryKeyAutoIncrement) 
        		/*&& !((database instanceof InformixDatabase)*/ && isSinglePrimaryKeyColumn))/*)*/ {
            // ...skip this code block for sqlite if a single column primary key
            // with an autoincrement constraint exists.
            // This constraint is added after the column type.

            if (statement.getPrimaryKeyConstraint() != null && statement.getPrimaryKeyConstraint().getColumns().size() > 0) {
                buffer.append(" PRIMARY KEY (");
                buffer.append(StringUtils.join(primaryKeyColumns, ", "));
                buffer.append(")");
                // Setting up table space for PK's index if it exist
                buffer.append(",");
            }
        }

        for (ForeignKeyConstraint fkConstraint : statement.getForeignKeyConstraints()) {
            String referencesString = fkConstraint.getReferences();
            if (!referencesString.contains(".") && database.getDefaultSchemaName() != null) {
                referencesString = database.getDefaultSchemaName()+"."+referencesString;
            }
            buffer.append(" FOREIGN KEY (")
                .append(StringUtils.join(fkConstraint.getColumnNames(), ", "))
                .append(") REFERENCES ")
                .append(referencesString);

            if (fkConstraint.getDeleteCascade()) {
                buffer.append(" ON DELETE CASCADE");
            }

            buffer.append(" CONSTRAINT ");
            buffer.append(database.escapeConstraintName(fkConstraint.getForeignKeyName()));

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

        String sql = buffer.toString().replaceFirst(",\\s*$", "") + ")";

        if (statement.getTablespace() != null && database.supportsTablespaces()) {
            sql += " IN " + statement.getTablespace();
        }

        return new Action[] { new UnparsedSql(sql) };
	}

	private boolean constraintNameAfterUnique(Database database) {
		return true;
	}
}
