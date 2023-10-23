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
import liquibase.statement.core.TableIsEmptyStatement;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TableIsEmptyPrecondition extends AbstractPrecondition {

    private String catalogName;

    private String schemaName;

    private String tableName;

    @Override
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet, ChangeExecListener changeExecListener) throws PreconditionFailedException, PreconditionErrorException {
        try {
            TableIsEmptyStatement statement = new TableIsEmptyStatement(getCatalogName(), getSchemaName(), getTableName());

            int result = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database).queryForInt(statement);
            if (result > 0) {
                throw new PreconditionFailedException("Table " + getTableName() + " is not empty.", changeLog, this);
            }

        } catch (PreconditionFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    @Override
    public Warnings warn(Database database) {
        return new Warnings();
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", tableName);

        return validationErrors;
    }


    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public String getName() {
        return "tableIsEmpty";
    }

}
