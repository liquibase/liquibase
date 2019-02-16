package liquibase.exception

import liquibase.change.ColumnConfig
import liquibase.change.core.CreateTableChange
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.changelog.RanChangeSet
import liquibase.changelog.visitor.ValidatingVisitor
import liquibase.database.Database
import liquibase.precondition.core.NotPrecondition
import liquibase.precondition.core.PreconditionContainer
import liquibase.sdk.database.MockDatabase
import spock.lang.Specification

/**
 * Tests for correct behaviour of non-trivial exception class ValidationFailedException
 */
class ValidationFailedExceptionTest extends Specification {
    ValidatingVisitor handler;
    ChangeSet changeSet1;
    ChangeSet changeSet2;
    ChangeSet duplicateChangeSet;
    ChangeSet setupExceptionChangeSet;
    ChangeSet validationErrorsChangeSet;
    ChangeSet validationFailedChangeSet;
    ChangeSet precondErrorChangeSet;
    ChangeSet precondFailedChangeSet;

    CreateTableChange change1;
    ColumnConfig column1;
    CreateTableChange change2;
    ValidationFailedException exception;


    void setup() {
        changeSet1 = new ChangeSet("1", "testAuthor", false, false, "path/changelog", null, null, null);
        changeSet2 = new ChangeSet("2", "testAuthor", false, false, "path/changelog", null, null, null);;
        duplicateChangeSet = new ChangeSet("duplicateChangeSet", "testAuthor", false, false, "path/changelog", null, null, null);        change1 = new CreateTableChange();
        setupExceptionChangeSet = new ChangeSet("setupExceptionChangeSet", "testAuthor", false, false, "path/changelog", null, null, null);        change1 = new CreateTableChange();
        validationErrorsChangeSet  = new ChangeSet("validationErrorsChangeSet", "testAuthor", false, false, "path/changelog", null, null, null);
        precondErrorChangeSet = new ChangeSet("precondErrorChangeSet", "testAuthor", false, false, "path/changelog", null, null, null);        change1 = new CreateTableChange();
        precondFailedChangeSet  = new ChangeSet("precondFailedChangeSet", "testAuthor", false, false, "path/changelog", null, null, null);

        change1 = new CreateTableChange();
        change1.setTableName("table1");
        column1 = new ColumnConfig();
        change1.addColumn(column1);
        column1.setName("col1");
        column1.setType("int");

        change2 = new CreateTableChange();
        change2.setTableName("table2");
        ColumnConfig column2 = new ColumnConfig();
        change2.addColumn(column2);
        column2.setName("col2");
        column2.setType("int");

        changeSet1.addChange(change1);
        changeSet2.addChange(change2);

        handler = new ValidatingVisitor(new ArrayList<RanChangeSet>());
        // Duplicate
        handler.visit(duplicateChangeSet, new DatabaseChangeLog(), new MockDatabase(), null);
        handler.visit(duplicateChangeSet, new DatabaseChangeLog(), new MockDatabase(), null);
        // Invalid MD5 sum
        handler.invalidMD5Sums.add("invalidMd5SumChangeSet");
        // Setup exception
        setupExceptionChangeSet.addChange(new CreateTableChange() {
            @Override
            public void finishInitialization() throws SetupException {
                throw new SetupException("setupExceptionChangeSet message");
            }
        });
        handler.visit(setupExceptionChangeSet, new DatabaseChangeLog(), null, null);
        // Validation failed
        validationErrorsChangeSet.addChange(new CreateTableChange() {
            @Override
            public ValidationErrors validate(Database database) {
                ValidationErrors changeValidationErrors = new ValidationErrors();
                changeValidationErrors.addError("validationErrorsChangeSet message");
                return changeValidationErrors;
            }
        });
        // PreConditions - errors
        precondErrorChangeSet.addChange(change1)
        precondErrorChangeSet.setPreconditions((new PreconditionContainer().addNestedPrecondition(new NotPrecondition()) ))
        // PreConditions - failed
        handler.visit(validationErrorsChangeSet, new DatabaseChangeLog(), null, null);

        exception = new ValidationFailedException(handler)
    }

    def "change set with invalid MD5 sum present in validation error message" () {
        when:
        def message = exception.getMessage()

        then:
        message.contains("invalidMd5SumChangeSet")
    }

    def "duplicate change set present in validation error message" () {
        when:
        def message = exception.getMessage()

        then:
        message.contains("duplicateChangeSet")
    }

    def "setup exception ChangeSet present in validation error message" () {
        when:
        def message = exception.getMessage()

        then:
        message.contains("setupExceptionChangeSet message")
    }

    def "validation failed ChangeSet present in validation error message" () {
        when:
        def message = exception.getMessage()

        then:
        message.contains("validationErrorsChangeSet message")
    }

    def "getInvalidMD5Sums returns the ChangeSet that was added"() {
        when:
        handler.invalidMD5Sums.add("invalidMd5SumChangeSet");
        def exception = new ValidationFailedException(handler)

        String message = exception.getInvalidMD5Sums()


        then:
        message.contains("invalidMd5SumChangeSet") == true
    }

}
