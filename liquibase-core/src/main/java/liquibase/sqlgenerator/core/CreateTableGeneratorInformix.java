package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.AutoIncrementConstraint;
import liquibase.statement.ForeignKeyConstraint;
import liquibase.statement.PrimaryKeyConstraint;
import liquibase.statement.UniqueConstraint;
import liquibase.statement.core.CreateTableStatement;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.util.StringUtil;

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
    public boolean supports(CreateTableStatement statement, Database database) {
        return database instanceof InformixDatabase;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE + 1;
    }

    /**
     * Informix SQL-specific implementation of the CREATE TABLE SQL generator.
     * @param statement The properties of the statement that we will translate into SQL
     * @param database For this implementation always an object of the InformixDatabase type
     * @param sqlGeneratorChain Other generators in the pipeline for this command
     * @return An array of Sql[] statements containing the requested SQL statements for Informix SQL
     */
    @Override
    public Sql[] generateSql(CreateTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder buffer = new StringBuilder();

        // CREATE TABLE table_name ...
        buffer.append("CREATE TABLE ")
                .append(database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()))
                .append(" ");
        buffer.append("(");

        Iterator<String> columnIterator = statement.getColumns().iterator();
        List<String> primaryKeyColumns = new LinkedList<>();

        /*
         * Build the list of columns and constraints in the form
         * (
         *   column1,
         *   ...,
         *   columnN,
         *   constraint1,
         *   ...,
         *   constraintN
         * )
         */
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
            boolean isPrimaryKeyColumn = (statement.getPrimaryKeyConstraint() != null) && statement
                .getPrimaryKeyConstraint().getColumns().contains(column);

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
                
                    if (!autoIncrementClause.isEmpty()) {
                        buffer.append(" ").append(autoIncrementClause);
                    }
                } else {
                    LogService.getLog(getClass()).warning(LogType.LOG, database.getShortName()+" does not support autoincrement columns as requested for "+(database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())));
                }
            }

            if (statement.getNotNullColumns().get(column) != null) {
                buffer.append(" NOT NULL");
            }

            if (columnIterator.hasNext()) {
                buffer.append(", ");
            }
        }

        buffer.append(",");

        /*
         * We only create a PRIMARY KEY constraint if one is defined and has at least 1 column.
         * General Informix SQL syntax, according to the docs for 11.5
         * https://www.ibm.com/support/knowledgecenter/SSGU8G_11.50.0/com.ibm.sqls.doc/ids_sqs_0100.htm
         * is:
         * ( columns ... --> PRIMARY KEY (column1, ..., columnN) [CONSTRAINT pk_name]
          */
        //

        PrimaryKeyConstraint pkConstraint = statement.getPrimaryKeyConstraint();
        if ((statement.getPrimaryKeyConstraint() != null) && !statement.getPrimaryKeyConstraint().getColumns().isEmpty()) {
            buffer.append(" PRIMARY KEY (");
            buffer.append(StringUtil.join(primaryKeyColumns, ", "));
            buffer.append(")");

            if (! StringUtil.isEmpty(pkConstraint.getConstraintName() )) {
                buffer.append(" CONSTRAINT ");
                buffer.append(database.escapeConstraintName(pkConstraint.getConstraintName()));
            }
            // Setting up table space for PK's index if it exist
            buffer.append(",");
        }

        for (ForeignKeyConstraint fkConstraint : statement.getForeignKeyConstraints()) {
            String referencesString = fkConstraint.getReferences();
            if (!referencesString.contains(".") && (database.getDefaultSchemaName() != null)) {
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

        // TODO: code duplication. Maybe we can merge this whole class into CreateTableGenerator again.
        for (UniqueConstraint uniqueConstraint : statement.getUniqueConstraints()) {
            if ((uniqueConstraint.getConstraintName() != null) && !constraintNameAfterUnique(database)) {
                buffer.append(" CONSTRAINT ");
                buffer.append(database.escapeConstraintName(uniqueConstraint.getConstraintName()));
            }
            buffer.append(" UNIQUE (");
            buffer.append(database.escapeColumnNameList(StringUtil.join(uniqueConstraint.getColumns(), ", ")));
            buffer.append(")");
            if ((uniqueConstraint.getConstraintName() != null) && constraintNameAfterUnique(database)) {
                buffer.append(" CONSTRAINT ");
                buffer.append(database.escapeConstraintName(uniqueConstraint.getConstraintName()));
            }
            buffer.append(",");
        }

        /*
         * Here, the list of columns and constraints in the form
         * ( column1, ..., columnN, constraint1, ..., constraintN,
         * ends. We cannot leave an expression like ", )", so we remove the last comma.
         */
        String sql = buffer.toString().replaceFirst(",\\s*$", "") + ")";

        if ((statement.getTablespace() != null) && database.supportsTablespaces()) {
            sql += " IN " + statement.getTablespace();
        }

        return new Sql[] { new UnparsedSql(sql, new Table().setName(statement.getTableName()).setSchema(new Schema(statement.getCatalogName(), statement.getSchemaName()))) };
    }

    private boolean constraintNameAfterUnique(Database database) {
        return true;
    }
}
