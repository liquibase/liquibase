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

    static final String LABELS_SIZE = "255";
    static final String CONTEXTS_SIZE = "255";

    List<SqlStatement> changeLogTableUpdate(Database database, Table changeLogTable) throws DatabaseException {

        Executor executor = ExecutorService.getInstance().getExecutor(database);

        List<SqlStatement> statementsToExecute = new ArrayList<>();

        String liquibaseCatalogName = database.getLiquibaseCatalogName();
        String liquibaseSchemaName = database.getLiquibaseSchemaName();
        String databaseChangeLogTableName = database.getDatabaseChangeLogTableName();
        String charTypeName = database.getCharTypeName();

        boolean hasDescription = changeLogTable.getColumn("DESCRIPTION") != null;
        if (!hasDescription) {
            executor.comment("Adding missing databasechangelog.description column");
            statementsToExecute.add(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName,
                    databaseChangeLogTableName, "DESCRIPTION", charTypeName + "(255)", null));
        }

        boolean hasTag = changeLogTable.getColumn("TAG") != null;
        if (!hasTag) {
            executor.comment("Adding missing databasechangelog.tag column");
            statementsToExecute.add(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName,
                    databaseChangeLogTableName, "TAG", charTypeName + "(255)", null));
        }

        boolean hasComments = changeLogTable.getColumn("COMMENTS") != null;
        if (!hasComments) {
            executor.comment("Adding missing databasechangelog.comments column");
            statementsToExecute.add(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName,
                    databaseChangeLogTableName, "COMMENTS", charTypeName + "(255)", null));
        }

        boolean hasLiquibase = changeLogTable.getColumn("LIQUIBASE") != null;
        if (!hasLiquibase) {
            executor.comment("Adding missing databasechangelog.liquibase column");
            statementsToExecute.add(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName,
                    databaseChangeLogTableName, "LIQUIBASE", charTypeName + "(20)",
                    null));
        } else {
            boolean liquibaseColumnNotRightSize = false;
            if (!(database instanceof SQLiteDatabase)) {
                DataType type = changeLogTable.getColumn("LIQUIBASE").getType();
                if (type.getTypeName().toLowerCase().startsWith("varchar")) {
                    Integer columnSize = type.getColumnSize();
                    liquibaseColumnNotRightSize = (columnSize != null) && (columnSize < 20);
                }
            }
            if (liquibaseColumnNotRightSize) {
                executor.comment("Modifying size of databasechangelog.liquibase column");

                statementsToExecute.add(new ModifyDataTypeStatement(liquibaseCatalogName, liquibaseSchemaName,
                        databaseChangeLogTableName, "LIQUIBASE", charTypeName + "(20)"));
            }
        }

        boolean hasMD5SUM = changeLogTable.getColumn("MD5SUM") != null;
        if (!hasMD5SUM) {
            executor.comment("Adding missing databasechangelog.liquibase column");
            statementsToExecute.add(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName,
                    databaseChangeLogTableName, "MD5SUM", charTypeName + "(35)",
                    null));
        } else {
            boolean checksumNotRightSize = false;
            if (!(database instanceof SQLiteDatabase)) {
                DataType type = changeLogTable.getColumn("MD5SUM").getType();
                if (type.getTypeName().toLowerCase().startsWith("varchar")) {
                    Integer columnSize = type.getColumnSize();
                    checksumNotRightSize = (columnSize != null) && (columnSize < 35);
                }
            }

            if (checksumNotRightSize) {
                executor.comment("Modifying size of databasechangelog.md5sum column");

                statementsToExecute.add(new ModifyDataTypeStatement(liquibaseCatalogName, liquibaseSchemaName,
                        databaseChangeLogTableName, "MD5SUM", charTypeName + "(35)"));
            }
        }

        boolean hasOrderExecuted = changeLogTable.getColumn("ORDEREXECUTED") != null;
        if (!hasOrderExecuted) {
            executor.comment("Adding missing databasechangelog.orderexecuted column");
            statementsToExecute.add(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName,
                    databaseChangeLogTableName, "ORDEREXECUTED", "int", null));
            statementsToExecute.add(new UpdateStatement(liquibaseCatalogName, liquibaseSchemaName,
                    databaseChangeLogTableName).addNewColumnValue("ORDEREXECUTED", -1));
            statementsToExecute.add(new SetNullableStatement(liquibaseCatalogName, liquibaseSchemaName,
                    databaseChangeLogTableName, "ORDEREXECUTED", "int", false));
        }

        boolean hasExecTypeColumn = changeLogTable.getColumn("EXECTYPE") != null;
        if (!hasExecTypeColumn) {
            executor.comment("Adding missing databasechangelog.exectype column");
            statementsToExecute.add(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName,
                    databaseChangeLogTableName, "EXECTYPE", charTypeName + "(10)",
                    null));
            statementsToExecute.add(new UpdateStatement(liquibaseCatalogName, liquibaseSchemaName,
                    databaseChangeLogTableName).addNewColumnValue("EXECTYPE", "EXECUTED"));
            statementsToExecute.add(new SetNullableStatement(liquibaseCatalogName, liquibaseSchemaName,
                    databaseChangeLogTableName, "EXECTYPE", charTypeName + "(10)",
                    false));
        }

        boolean hasContexts = changeLogTable.getColumn("CONTEXTS") != null;
        if (hasContexts) {
            Integer columnSize = changeLogTable.getColumn("CONTEXTS").getType().getColumnSize();
            if ((columnSize != null) && (columnSize < Integer.parseInt(CONTEXTS_SIZE))) {
                executor.comment("Modifying size of databasechangelog.contexts column");
                statementsToExecute.add(new ModifyDataTypeStatement(liquibaseCatalogName,
                        liquibaseSchemaName, databaseChangeLogTableName, "CONTEXTS",
                        charTypeName + "("+ CONTEXTS_SIZE+")"));
            }
        } else {
            executor.comment("Adding missing databasechangelog.contexts column");
            statementsToExecute.add(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName,
                    databaseChangeLogTableName, "CONTEXTS", charTypeName + "("
                    + CONTEXTS_SIZE+")", null));
        }

        boolean hasLabels = changeLogTable.getColumn("LABELS") != null;
        if (hasLabels) {
            Integer columnSize = changeLogTable.getColumn("LABELS").getType().getColumnSize();
            if ((columnSize != null) && (columnSize < Integer.parseInt(LABELS_SIZE))) {
                executor.comment("Modifying size of databasechangelog.labels column");
                statementsToExecute.add(new ModifyDataTypeStatement(liquibaseCatalogName,
                        liquibaseSchemaName, databaseChangeLogTableName, "LABELS",
                        charTypeName + "(" + LABELS_SIZE + ")"));
            }
        } else {
            executor.comment("Adding missing databasechangelog.labels column");
            statementsToExecute.add(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName,
                    databaseChangeLogTableName, "LABELS", charTypeName + "(" +
                    LABELS_SIZE + ")", null));
        }

        boolean hasDeploymentIdColumn = changeLogTable.getColumn("DEPLOYMENT_ID") != null;
        if (!hasDeploymentIdColumn) {
            executor.comment("Adding missing databasechangelog.deployment_id column");
            statementsToExecute.add(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName,
                    databaseChangeLogTableName, "DEPLOYMENT_ID", "VARCHAR(10)", null));
            if (database instanceof DB2Database) {
                statementsToExecute.add(new ReorganizeTableStatement(liquibaseCatalogName,
                        liquibaseSchemaName, databaseChangeLogTableName));
            }
        }

        return statementsToExecute;
    }

}
