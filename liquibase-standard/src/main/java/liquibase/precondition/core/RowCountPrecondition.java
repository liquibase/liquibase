package liquibase.precondition.core;

import liquibase.Scope;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.executor.ExecutorService;
import liquibase.precondition.AbstractPrecondition;
import liquibase.statement.core.TableRowCountStatement;
import liquibase.util.StringUtil;

public class RowCountPrecondition extends AbstractPrecondition {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private Long expectedRows;

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtil.trimToNull(schemaName);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Long getExpectedRows() {
        return expectedRows;
    }

    public void setExpectedRows(Long expectedRows) {
        this.expectedRows = expectedRows;
    }

    @Override
    public Warnings warn(Database database) {
        return new Warnings();
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", tableName);
        validationErrors.checkRequiredField("expectedRows", expectedRows);

        return validationErrors;
    }

    @Override
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet, ChangeExecListener changeExecListener)
            throws PreconditionFailedException, PreconditionErrorException {
        try {
            TableRowCountStatement statement = new TableRowCountStatement(catalogName, schemaName, tableName);

            long result = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database).queryForLong(statement);
            if (result != expectedRows) {
                throw new PreconditionFailedException(getFailureMessage(result, expectedRows), changeLog, this);
            }

        } catch (PreconditionFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    protected String getFailureMessage(long result, long expectedRows) {
        return "Table "+tableName+" does not have the expected row count of "+expectedRows+". It contains "+result+" rows";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public String getName() {
        return "rowCount";
    }

}
