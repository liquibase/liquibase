package liquibase.changelog;

import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.DatabaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.*;
import liquibase.structure.core.DataType;
import liquibase.structure.core.Table;

import java.util.ArrayList;
import java.util.List;

public class StandardChangeLogHistorySqlStatementGenerator {

    static final int LABELS_SIZE = 255;
    static final int CONTEXTS_SIZE = 255;
    public static final int DESCRIPTON_SIZE = 255;
    public static final int TAG_SIZE = 255;
    public static final int COMMENTS_SIZE = 255;
    public static final int LIQUIBASE_SIZE = 20;
    public static final int MD5SUM_SIZE = 35;
    public static final int EXECTYPE_SIZE = 10;

    List<SqlStatement> changeLogTableUpdate(Database database, Table changeLogTable) throws DatabaseException {
        List<SqlStatement> statementsToExecute = new ArrayList<>();

        String charTypeName = database.getCharTypeName();

        statementsToExecute.addAll(complementWithSimpleTextColumn(database, changeLogTable, "DESCRIPTION", DESCRIPTON_SIZE));

        statementsToExecute.addAll(complementWithSimpleTextColumn(database, changeLogTable, "TAG", TAG_SIZE));

        statementsToExecute.addAll(complementWithSimpleTextColumn(database, changeLogTable, "COMMENTS", COMMENTS_SIZE));

        statementsToExecute.addAll(complementWithShortTextColumn(database, changeLogTable, "LIQUIBASE", LIQUIBASE_SIZE));

        statementsToExecute.addAll(complementWithShortTextColumn(database, changeLogTable, "MD5SUM", MD5SUM_SIZE));

        statementsToExecute.addAll(complementWithNonEmptyColumn(database, changeLogTable, "ORDEREXECUTED", -1, "int"));

        statementsToExecute.addAll(complementWithNonEmptyColumn(database, changeLogTable, "EXECTYPE", "EXECUTED", charTypeName + "(" + EXECTYPE_SIZE + ")"));

        statementsToExecute.addAll(complementWithSimpleTextColumnTypeAware(database, changeLogTable, "CONTEXTS", CONTEXTS_SIZE));

        statementsToExecute.addAll(complementWithSimpleTextColumnTypeAware(database, changeLogTable, "LABELS", LABELS_SIZE));

        statementsToExecute.addAll(complementWithDeploymentIdColumn(database, changeLogTable, "DEPLOYMENT_ID", 10));

        return statementsToExecute;
    }

    private List<SqlStatement> complementWithDeploymentIdColumn(Database database, Table changeLogTable, String columnName, int desiredColumnSize) throws DatabaseException {
        List<SqlStatement> sqlStatements = new ArrayList<>();
        String liquibaseCatalogName = database.getLiquibaseCatalogName();
        String liquibaseSchemaName = database.getLiquibaseSchemaName();
        String databaseChangeLogTableName = database.getDatabaseChangeLogTableName();
        Executor executor = ExecutorService.getInstance().getExecutor(database);


        boolean hasDeploymentIdColumn = changeLogTable.getColumn(columnName) != null;
        if (!hasDeploymentIdColumn) {
            executor.comment("Adding missing databasechangelog." + columnName + " column");
            sqlStatements.add(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName,
                    databaseChangeLogTableName, columnName, "VARCHAR(" + desiredColumnSize + ")", null));
            if (database instanceof DB2Database) {
                sqlStatements.add(new ReorganizeTableStatement(liquibaseCatalogName,
                        liquibaseSchemaName, databaseChangeLogTableName));
            }
        }

        return sqlStatements;
    }

    private List<SqlStatement> complementWithSimpleTextColumnTypeAware(Database database, Table changeLogTable, String columnName, int desiredColumnSize) throws DatabaseException {
        List<SqlStatement> sqlStatements = new ArrayList<>();
        String liquibaseCatalogName = database.getLiquibaseCatalogName();
        String liquibaseSchemaName = database.getLiquibaseSchemaName();
        String databaseChangeLogTableName = database.getDatabaseChangeLogTableName();
        String charTypeName = database.getCharTypeName();
        Executor executor = ExecutorService.getInstance().getExecutor(database);

        boolean hasContexts = changeLogTable.getColumn(columnName) != null;
        if (hasContexts) {
            Integer columnSize = changeLogTable.getColumn(columnName).getType().getColumnSize();
            if ((columnSize != null) && (columnSize < desiredColumnSize)) {
                executor.comment("Modifying size of databasechangelog." + columnName + " column");
                sqlStatements.add(new ModifyDataTypeStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, columnName, charTypeName + "(" + desiredColumnSize + ")"));
            }
        } else {
            executor.comment("Adding missing databasechangelog." + columnName + " column");
            sqlStatements.add(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, columnName, charTypeName + "(" + desiredColumnSize + ")", null));
        }

        return sqlStatements;
    }

    private List<SqlStatement> complementWithNonEmptyColumn(Database database, Table changeLogTable, String columnName, Object defaultValue, String columnDataType) throws DatabaseException {
        List<SqlStatement> sqlStatements = new ArrayList<>();
        String liquibaseCatalogName = database.getLiquibaseCatalogName();
        String liquibaseSchemaName = database.getLiquibaseSchemaName();
        String databaseChangeLogTableName = database.getDatabaseChangeLogTableName();
        Executor executor = ExecutorService.getInstance().getExecutor(database);

        boolean columnExists = changeLogTable.getColumn(columnName) != null;
        if (!columnExists) {
            executor.comment("Adding missing databasechangelog." + columnName + " column");
            sqlStatements.add(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, columnName, columnDataType, null));
            sqlStatements.add(new UpdateStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName).addNewColumnValue(columnName, defaultValue));
            sqlStatements.add(new SetNullableStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, columnName, columnDataType, false));
        }

        return sqlStatements;
    }

    private List<SqlStatement> complementWithShortTextColumn(Database database, Table changeLogTable, String columnName, int desiredColumnSize) throws DatabaseException {
        List<SqlStatement> statements = new ArrayList<>();
        String liquibaseCatalogName = database.getLiquibaseCatalogName();
        String liquibaseSchemaName = database.getLiquibaseSchemaName();
        String databaseChangeLogTableName = database.getDatabaseChangeLogTableName();
        String charTypeName = database.getCharTypeName();
        Executor executor = ExecutorService.getInstance().getExecutor(database);
        String columnType = charTypeName + "(" + desiredColumnSize + ")";


        boolean columnExists = changeLogTable.getColumn(columnName) != null;
        if (!columnExists) {
            executor.comment("Adding missing databasechangelog." + columnName + " column");
            statements.add(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, columnName, columnType, null));
        } else {
            boolean liquibaseColumnNotRightSize = false;
            if (!(database instanceof SQLiteDatabase)) {
                DataType type = changeLogTable.getColumn(columnName).getType();
                if (type.getTypeName().toLowerCase().startsWith("varchar")) {
                    Integer columnSize = type.getColumnSize();
                    liquibaseColumnNotRightSize = (columnSize != null) && (columnSize < desiredColumnSize);
                }
            }
            if (liquibaseColumnNotRightSize) {
                executor.comment("Modifying size of databasechangelog." + columnName + " column");

                statements.add(new ModifyDataTypeStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, columnName, columnType));
            }
        }

        return statements;
    }

    private List<SqlStatement> complementWithSimpleTextColumn(Database database, Table changeLogTable, String columnName, int desiredColumnSize) throws DatabaseException {
        List<SqlStatement> statements = new ArrayList<>();
        String liquibaseCatalogName = database.getLiquibaseCatalogName();
        String liquibaseSchemaName = database.getLiquibaseSchemaName();
        String databaseChangeLogTableName = database.getDatabaseChangeLogTableName();
        String charTypeName = database.getCharTypeName();
        Executor executor = ExecutorService.getInstance().getExecutor(database);
        String columnType = charTypeName + "(" + desiredColumnSize + ")";

        boolean columnExists = changeLogTable.getColumn(columnName) != null;
        if (!columnExists) {
            executor.comment("Adding missing databasechangelog." + columnName + " column");
            statements.add(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, columnName, columnType, null));
        }

        return statements;
    }

}
