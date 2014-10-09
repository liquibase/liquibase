package liquibase.sqlgenerator.core;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.logging.LogFactory;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.AutoIncrementConstraint;
import liquibase.statement.ForeignKeyConstraint;
import liquibase.statement.UniqueConstraint;
import liquibase.statement.core.CreateTableStatement;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;


/**
 * An Informix-specific create table statement generator.
 * 
 * @author islavov
 */
public class CreateTableGeneratorInformix extends CreateTableGenerator {

    @Override
    public boolean supports(CreateTableStatement statement, Database database) {
        return database instanceof InformixDatabase;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

	@Override
    public Sql[] generateSql(CreateTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
		StringBuilder buffer = new StringBuilder();

        buffer.append("CREATE TABLE ").append(database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())).append(" ");
        buffer.append("(");
        
        boolean isSinglePrimaryKeyColumn = 
        		statement.getPrimaryKeyConstraint() != null && 
        		statement.getPrimaryKeyConstraint().getColumns().size() == 1;
        
        boolean isPrimaryKeyAutoIncrement = false;
        
        Iterator<String> columnIterator = statement.getColumns().iterator();
        List<String> primaryKeyColumns = new LinkedList<String>();
        while (columnIterator.hasNext()) {
            String column = columnIterator.next();
            
            buffer.append(database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), column));
            buffer.append(" ").append(statement.getColumnTypes().get(column).toDatabaseDataType(database).toSql());
            
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
                buffer.append(statement.getColumnTypes().get(column).objectToSql(defaultValue, database));
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

            if (statement.getNotNullColumns().contains(column)) {
                buffer.append(" NOT NULL");
            }

            if (columnIterator.hasNext()) {
                buffer.append(", ");
            }
        }

        buffer.append(",");

        // TODO informixdb
        // Fix according to: https://liquibase.jira.com/browse/CORE-1775
        if (isSinglePrimaryKeyColumn && isPrimaryKeyAutoIncrement) {

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
                .append(database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), fkConstraint.getColumn()))
                .append(") REFERENCES ")
                .append(referencesString);

            if (fkConstraint.isDeleteCascade()) {
                buffer.append(" ON DELETE CASCADE");
            }

            buffer.append(" CONSTRAINT ");
            buffer.append(database.escapeConstraintName(fkConstraint.getForeignKeyName()));

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

        String sql = buffer.toString().replaceFirst(",\\s*$", "") + ")";

        if (statement.getTablespace() != null && database.supportsTablespaces()) {
            sql += " IN " + statement.getTablespace();
        }

        return new Sql[] { new UnparsedSql(sql, new Table().setName(statement.getTableName()).setSchema(new Schema(statement.getCatalogName(), statement.getSchemaName()))) };
	}

	private boolean constraintNameAfterUnique(Database database) {
		return true;
	}
}
