package liquibase.changelog.visitor

import liquibase.GlobalConfiguration
import liquibase.Scope
import liquibase.change.ColumnConfig
import liquibase.change.core.CreateTableChange
import liquibase.change.core.OutputChange
import liquibase.change.core.SQLFileChange
import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.changelog.RanChangeSet
import liquibase.database.Database
import liquibase.database.core.MockDatabase
import liquibase.exception.SetupException
import liquibase.exception.ValidationErrors
import spock.lang.Specification

class ValidatingVisitorTest extends Specification {

     void "validate successful visit"() throws Exception {
         when:
         def changeSet1 = new ChangeSet("1", "testAuthor", false, false, "path/changelog", null, null, null)
         def changeSet2 = new ChangeSet("2", "testAuthor", false, false, "path/changelog", null, null, null)

        CreateTableChange change1 = new CreateTableChange()
        change1.setTableName("table1")
        ColumnConfig column1 = new ColumnConfig()
        change1.addColumn(column1)
        column1.setName("col1")
        column1.setType("int")

        CreateTableChange change2 = new CreateTableChange()
        change2.setTableName("table2")
        ColumnConfig column2 = new ColumnConfig()
        change2.addColumn(column2)
        column2.setName("col2")
        column2.setType("int")

        changeSet1.addChange(change1)
        changeSet2.addChange(change2)

        ValidatingVisitor handler = new ValidatingVisitor(new ArrayList<RanChangeSet>())
        handler.visit(changeSet1, new DatabaseChangeLog(), new MockDatabase(), null)
        handler.visit(changeSet2, new DatabaseChangeLog(), new MockDatabase(), null)

        then:
        handler.validationPassed()
    }

    void "validate visit setup exception"() throws Exception {
        when:
        def changeSet1 = new ChangeSet("1", "testAuthor", false, false, "path/changelog", null, null, null)
        changeSet1.addChange(new CreateTableChange() {
            @Override
            void finishInitialization() throws SetupException {
                throw new SetupException("Test message")
            }
        });

        ValidatingVisitor handler = new ValidatingVisitor(new ArrayList<RanChangeSet>())
        handler.visit(changeSet1, new DatabaseChangeLog(), null, null)

        then:
        handler.getSetupExceptions().size() == 1
        handler.getSetupExceptions().get(0).getMessage().equalsIgnoreCase("Test message")
        !handler.validationPassed()
    }

    void "validate visit error"() throws Exception {
        when:
        def changeSet1 = new ChangeSet("1", "testAuthor", false, false, "path/changelog", null, null, null)
        changeSet1.addChange(new CreateTableChange() {
            @Override
            ValidationErrors validate(Database database) {
                ValidationErrors changeValidationErrors = new ValidationErrors()
                changeValidationErrors.addError("Test message")
                return changeValidationErrors;
            }
        })

        List<RanChangeSet> ran = new ArrayList<RanChangeSet>()
        ValidatingVisitor handler = new ValidatingVisitor(ran)
        handler.visit(changeSet1, new DatabaseChangeLog(), null, null)

        then:
        handler.getValidationErrors().getErrorMessages().size() == 1
        handler.getValidationErrors().getErrorMessages().get(0).startsWith("Test message")
        !handler.validationPassed()
    }

    void "validate visit duplicate"() throws Exception {
        when:
        def changeSet1 = new ChangeSet("1", "testAuthor", false, false, "path/changelog", null, null, null)
        ValidatingVisitor handler = new ValidatingVisitor(new ArrayList<RanChangeSet>())
        handler.visit(changeSet1, new DatabaseChangeLog(), null, null)
        handler.visit(changeSet1, new DatabaseChangeLog(), null, null)

        then:
        handler.getDuplicateChangeSets().size() == 1
        !handler.validationPassed()
    }

    void "validate error on empty author when strict configuration is set as true"() {
        when:
        ChangeSet changeSet = new ChangeSet("emptyAuthor", "", false, false, "path/changelog", null, null, null)
        ValidatingVisitor handler = new ValidatingVisitor(new ArrayList<>())

        Scope.child([
                (GlobalConfiguration.STRICT.getKey()): Boolean.TRUE,
        ], new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {
                handler.visit(changeSet, new DatabaseChangeLog(), null, null)
            }
        })

        then:
         handler.getValidationErrors().getErrorMessages().get(0).contains("ChangeSet Author is empty")
    }

    void "validate there is no error on empty author when strict configuration is set as false"() {
        when:
        ChangeSet changeSet = new ChangeSet("emptyAuthor", "", false, false, "path/changelog", null, null, null)
        ValidatingVisitor handler = new ValidatingVisitor(new ArrayList<>())

        Scope.child([
                (GlobalConfiguration.STRICT.getKey()): Boolean.FALSE,
        ], new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {
                handler.visit(changeSet, new DatabaseChangeLog(), null, null)
            }
        })

        then:
        handler.validationPassed()
    }

    void "validate visit error on empty id"() throws Exception {
        when:
        def changeSet = new ChangeSet("", "emptyId", false, false, "path/changelog", null, null, null)
        ValidatingVisitor handler = new ValidatingVisitor(new ArrayList<>())
        handler.visit(changeSet, new DatabaseChangeLog(), null, null)

        then:
        handler.getValidationErrors().getErrorMessages().size() == 1
        handler.getValidationErrors().getErrorMessages().get(0).contains("ChangeSet Id is empty")
        !handler.validationPassed()
    }

    void "validate visit error on empty author and id"() throws Exception {
        when:
        def changeSet = new ChangeSet("", "", false, false, "path/changelog", null, null, null)
        ValidatingVisitor handler = new ValidatingVisitor(new ArrayList<>())
        handler.visit(changeSet, new DatabaseChangeLog(), null, null)

        then:
        handler.getValidationErrors().getErrorMessages().size() == 1
        handler.getValidationErrors().getErrorMessages().get(0).contains("ChangeSet Id and Author are empty")
        !handler.validationPassed()
    }

    void "validate visit to run only"() throws Exception {
        when:
        def changeSet = new ChangeSet("1", "testAuthor", false, false, "path/changelog", null, null, null);
        changeSet.addChange(new CreateTableChange() {
            @Override
            ValidationErrors validate(Database database) {
                ValidationErrors changeValidationErrors = new ValidationErrors()
                changeValidationErrors.addError("Test message")
                return changeValidationErrors;
            }
        })

        List<RanChangeSet> ran = new ArrayList<RanChangeSet>()
        ran.add(new RanChangeSet(changeSet))
        ValidatingVisitor handler = new ValidatingVisitor(ran)
        handler.visit(changeSet, new DatabaseChangeLog(), null, null)

        then:
        handler.getSetupExceptions().size() == 0
        handler.validationPassed()
    }

    void "validate successful visit with single valid dbms set"() throws Exception {
        when:
        CreateTableChange change = new CreateTableChange()
        change.setTableName("table1")
        ColumnConfig column1 = new ColumnConfig()
        change.addColumn(column1)
        column1.setName("col1")
        column1.setType("int")

        ChangeSet changeSet = new ChangeSet("1", "testAuthor", false, false, "path/changelog", null, "postgresql", null)
        changeSet.addChange(change)
        ValidatingVisitor handler = new ValidatingVisitor(new ArrayList<RanChangeSet>())
        handler.visit(changeSet, new DatabaseChangeLog(), new MockDatabase(), null)

        then:
        handler.validationPassed()
    }

    void "validate successful visit with valid list of dbms set"() throws Exception {
        when:
        CreateTableChange change = new CreateTableChange()
        change.setTableName("table1")
        ColumnConfig column1 = new ColumnConfig()
        change.addColumn(column1)
        column1.setName("col1")
        column1.setType("int")
        ChangeSet changeSet = new ChangeSet("1", "testAuthor", false, false, "path/changelog", null, "postgresql, mssql, h2", null)
        changeSet.addChange(change)

        ValidatingVisitor handler = new ValidatingVisitor(new ArrayList<RanChangeSet>())
        handler.visit(changeSet, new DatabaseChangeLog(), new MockDatabase(), null)

        then:
        handler.validationPassed()
    }

    void "validate unsuccessful visit with invalid dbms set"() throws Exception {
        when:
        CreateTableChange change = new CreateTableChange()
        change.setTableName("table1")
        ColumnConfig column1 = new ColumnConfig()
        change.addColumn(column1)
        column1.setName("col1")
        column1.setType("int")
        ChangeSet changeSet = new ChangeSet("1", "testAuthor", false, false, "path/changelog", null, "post", null)
        changeSet.addChange(change)

        ValidatingVisitor handler = new ValidatingVisitor(new ArrayList<RanChangeSet>())
        handler.visit(changeSet, new DatabaseChangeLog(), new MockDatabase(), null)

        then:
        !handler.validationPassed()
    }

    void "validate successful visit with none dbms set"() throws Exception {
        when:
        CreateTableChange change = new CreateTableChange()
        change.setTableName("table1")
        ColumnConfig column1 = new ColumnConfig()
        change.addColumn(column1)
        column1.setName("col1")
        column1.setType("int")
        ChangeSet changeSet = new ChangeSet("1", "testAuthor", false, false, "path/changelog", null, "none", null)
        changeSet.addChange(change)

        ValidatingVisitor handler = new ValidatingVisitor(new ArrayList<RanChangeSet>())
        handler.visit(changeSet, new DatabaseChangeLog(), new MockDatabase(), null)

        then:
        handler.validationPassed()
    }

    void "validate successful visit with all dbms value set"() throws Exception {
        when:
        CreateTableChange change = new CreateTableChange()
        change.setTableName("table1")
        ColumnConfig column1 = new ColumnConfig()
        change.addColumn(column1)
        column1.setName("col1")
        column1.setType("int")
        ChangeSet changeSet = new ChangeSet("1", "testAuthor", false, false, "path/changelog", null, "all", null)
        changeSet.addChange(change)

        ValidatingVisitor handler = new ValidatingVisitor(new ArrayList<RanChangeSet>())
        handler.visit(changeSet, new DatabaseChangeLog(), new MockDatabase(), null)

        then:
        handler.validationPassed()
    }

    void "validate successful visit when always run is true and the changeset is ran again after being modified"() throws Exception {
        when:
        def changeSet = new ChangeSet("1", "testAuthor", true, false, "path/changelog", null, null, null);
        def outputChange = new OutputChange()
        outputChange.message = "Hello World"
        changeSet.addChange(outputChange)
        List<RanChangeSet> ran = new ArrayList<RanChangeSet>()
        ran.add(new RanChangeSet(changeSet))

        ValidatingVisitor handler = new ValidatingVisitor(ran)

        def theSameChangeSetWithDifferentChanges = new ChangeSet("1", "testAuthor", true, false, "path/changelog", null, null, null);
        def outputChangeWithModifiedMessage = new OutputChange()
        outputChangeWithModifiedMessage.message = "Hello To You"
        theSameChangeSetWithDifferentChanges.addChange(outputChangeWithModifiedMessage)
        handler.visit(theSameChangeSetWithDifferentChanges, new DatabaseChangeLog(), new MockDatabase(), null)

        then:
        handler.getSetupExceptions().size() == 0
        handler.validationPassed()
    }

    void "validate successful visit when SQLFileChange checksum changes from expanding-properties to not expanding-properties"() throws Exception {
        when:
        def changeSet1 = new ChangeSet("1", "testAuthor", false, false, "path/changelog", null, null, null)
        def changeLogParameters1 = new ChangeLogParameters()
        changeLogParameters1.set("database.liquibaseSchemaName", "schema1")
        changeSet1.setChangeLogParameters(changeLogParameters1)
        def sqlFileChange1 = new SQLFileChange()
        sqlFileChange1.path = "com/example-2/fileWithSchemaNameProperty.sql"
        sqlFileChange1.relativeToChangelogFile = false
        sqlFileChange1.setChangeSet(changeSet1)
        sqlFileChange1.doExpandExpressionsInGenerateChecksum = true
        changeSet1.addChange(sqlFileChange1)
        List<RanChangeSet> ran = new ArrayList<RanChangeSet>()
        ran.add(new RanChangeSet(changeSet1))
        ran.get(0).setLiquibaseVersion("4.24.0")

        ValidatingVisitor handler = new ValidatingVisitor(ran)
        def changeSet2 = new ChangeSet("1", "testAuthor", false, false, "path/changelog", null, null, null)
        def changeLogParameters2 = new ChangeLogParameters()
        /*
         * Note that the changelog parameters must be the same as when
         * the changeset's checksum was stored. Otherwise, there's no way
         * to reproduce the original checksum.
         */
        changeLogParameters2.set("database.liquibaseSchemaName", "schema1")
        changeSet2.setChangeLogParameters(changeLogParameters2)
        def sqlFileChange2 = new SQLFileChange()
        sqlFileChange2.path = "com/example-2/fileWithSchemaNameProperty.sql"
        sqlFileChange2.relativeToChangelogFile = false
        sqlFileChange2.setChangeSet(changeSet2)
        sqlFileChange2.doExpandExpressionsInGenerateChecksum = false
        changeSet2.addChange(sqlFileChange2)
        handler.visit(changeSet2, new DatabaseChangeLog(), new MockDatabase(), null)

        then:
        handler.validationPassed()
    }
}
