package liquibase.parser.core.xml

import liquibase.Contexts
import liquibase.change.Change
import liquibase.change.ChangeFactory
import liquibase.change.CheckSum
import liquibase.change.core.*
import liquibase.change.custom.CustomChangeWrapper
import liquibase.change.custom.ExampleCustomSqlChange
import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.configuration.LiquibaseConfiguration
import liquibase.database.ObjectQuotingStrategy
import liquibase.sdk.database.MockDatabase
import liquibase.exception.ChangeLogParseException
import liquibase.precondition.CustomPreconditionWrapper
import liquibase.precondition.core.*
import liquibase.sdk.supplier.resource.ResourceSupplier
import liquibase.sql.visitor.AppendSqlVisitor
import liquibase.sql.visitor.ReplaceSqlVisitor
import liquibase.test.JUnitResourceAccessor
import spock.lang.FailsWith
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static org.hamcrest.Matchers.containsInAnyOrder
import static spock.util.matcher.HamcrestSupport.that

public class XMLChangeLogSAXParser_RealFile_Test extends Specification {

    @Shared resourceSupplier = new ResourceSupplier()

    def before() {
        LiquibaseConfiguration.getInstance().reset();
    }

    def "namespace configured correctly"() {
        expect:
        assert new XMLChangeLogSAXParser().saxParserFactory.isNamespaceAware()
        assert new XMLChangeLogSAXParser().saxParserFactory.isValidating()
    }

    def "supports method identifies xml files correctly"() {
        when:
        def parser = new XMLChangeLogSAXParser()

        then:
        assert parser.supports("text.xml", resourceSupplier.simpleResourceAccessor)
        assert parser.supports("text.XML", resourceSupplier.simpleResourceAccessor)
        assert parser.supports("text.Xml", resourceSupplier.simpleResourceAccessor)
        assert parser.supports("com/example/text.xml", resourceSupplier.simpleResourceAccessor)
        assert !parser.supports("com/example/text.yaml", resourceSupplier.simpleResourceAccessor)
        assert !parser.supports("com/example/text.sql", resourceSupplier.simpleResourceAccessor)
        assert !parser.supports("com/example/text.unknown", resourceSupplier.simpleResourceAccessor)

    }

    def "able to parse a simple changelog simpleChangeLog.xml"() throws Exception {
        def path = "liquibase/parser/core/xml/simpleChangeLog.xml"
        when:
        def changeLog = new XMLChangeLogSAXParser().parse(path, new ChangeLogParameters(), new JUnitResourceAccessor());
        def changeSet = changeLog.changeSets[0];
        def change = changeSet.changes[0];

        then:
        changeLog.getLogicalFilePath() == path
        changeLog.getPhysicalFilePath() == path

        changeLog.getPreconditions().getNestedPreconditions().size() == 0
        changeLog.getChangeSets().size() == 1

        changeSet.getAuthor() == "nvoxland"
        changeSet.getId() == "1"
        changeSet.getChanges().size() == 1
        changeSet.getFilePath() == path
        changeSet.getComments() == "Some comments go here"

        ChangeFactory.getInstance().getChangeMetaData(change).getName() == "createTable"
        assert change instanceof CreateTableChange
        change.tableName == "person"
        change.columns.size() == 3
        change.columns[0].name == "id"
        change.columns[0].type == "int"
        change.columns[0].constraints != null
        assert change.columns[0].constraints.primaryKey
        assert !change.columns[0].constraints.nullable

        change.columns.get(1).name == "firstname"
        change.columns.get(1).type == "varchar(50)"
        change.columns.get(1).constraints == null

        change.columns.get(2).name == "lastname"
        change.columns.get(2).type == "varchar(50)"
        change.columns.get(2).constraints != null
        assert !change.columns.get(2).constraints.primaryKey
        assert !change.columns.get(2).constraints.nullable
    }

    def "able to parse a changelog with multiple changeSets multiChangeSetChangeLog.xml"() throws Exception {
        def path = "liquibase/parser/core/xml/multiChangeSetChangeLog.xml"
        when:
        DatabaseChangeLog changeLog = new XMLChangeLogSAXParser().parse(path, new ChangeLogParameters(), new JUnitResourceAccessor());

        then:
        changeLog.getLogicalFilePath() == path
        changeLog.getPhysicalFilePath() == path

        changeLog.getPreconditions().getNestedPreconditions().size() == 0
        changeLog.getChangeSets().size() == 4

        changeLog.getChangeSets()[0].getAuthor() == "nvoxland"
        changeLog.getChangeSets()[0].getId() == "1"
        changeLog.getChangeSets()[0].getChanges().size() == 1
        changeLog.getChangeSets()[0].getFilePath() == path
        changeLog.getChangeSets()[0].getComments() == null
        assert !changeLog.getChangeSets()[0].shouldAlwaysRun()
        assert !changeLog.getChangeSets()[0].shouldRunOnChange()

        ChangeFactory.getInstance().getChangeMetaData(changeLog.getChangeSets()[0].getChanges()[0]).getName() == "createTable"
        assert changeLog.getChangeSets()[0].getChanges()[0] instanceof CreateTableChange

        then:
        changeLog.getChangeSets().get(1).getAuthor() == "nvoxland"
        changeLog.getChangeSets().get(1).getId() == "2"
        changeLog.getChangeSets().get(1).getChanges().size() == 2
        changeLog.getChangeSets().get(1).getFilePath() == path
        changeLog.getChangeSets().get(1).getComments() == "Testing add column"
        assert changeLog.getChangeSets().get(1).shouldAlwaysRun()
        assert changeLog.getChangeSets().get(1).shouldRunOnChange()
        changeLog.getChangeSets().get(1).getRollBackChanges().length == 2
        assert changeLog.getChangeSets().get(1).getRollBackChanges()[0] instanceof RawSQLChange
        assert changeLog.getChangeSets().get(1).getRollBackChanges()[1] instanceof RawSQLChange

        ChangeFactory.getInstance().getChangeMetaData(changeLog.getChangeSets().get(1).getChanges()[0]).getName() == "addColumn"
        assert changeLog.getChangeSets().get(1).getChanges()[0] instanceof AddColumnChange

        ChangeFactory.getInstance().getChangeMetaData(changeLog.getChangeSets().get(1).getChanges().get(1)).getName() == "addColumn"
        assert changeLog.getChangeSets().get(1).getChanges().get(1) instanceof AddColumnChange

        changeLog.getChangeSets().get(2).getAuthor() == "bob"
        changeLog.getChangeSets().get(2).getId() == "3"
        changeLog.getChangeSets().get(2).getChanges().size() == 1
        changeLog.getChangeSets().get(2).getFilePath() == path
        changeLog.getChangeSets().get(2).getComments() == null
        assert !changeLog.getChangeSets().get(2).shouldAlwaysRun()
        assert !changeLog.getChangeSets().get(2).shouldRunOnChange()

        ChangeFactory.getInstance().getChangeMetaData(changeLog.getChangeSets().get(2).getChanges()[0]).getName() == "createTable"
        assert changeLog.getChangeSets().get(2).getChanges()[0] instanceof CreateTableChange


        changeLog.getChangeSets().get(3).getChanges().size() == 1

        assert changeLog.getChangeSets().get(3).getChanges()[0] instanceof CustomChangeWrapper
        assert changeLog.getChangeSets().get(3).getChanges()[0].getCustomChange() instanceof ExampleCustomSqlChange
        changeLog.getChangeSets().get(3).getChanges()[0].generateStatements(new MockDatabase()) //fills out customChange params
        changeLog.getChangeSets().get(3).getChanges()[0].getCustomChange().getTableName() == "table"
        changeLog.getChangeSets().get(3).getChanges()[0].getCustomChange().getColumnName() == "column"
    }

    def "local path can be set in changelog file logicalPathChangeLog.xml"() throws Exception {
        when:
        def physicalPath = "liquibase/parser/core/xml/logicalPathChangeLog.xml"
        def changeLog = new XMLChangeLogSAXParser().parse(physicalPath, new ChangeLogParameters(), new JUnitResourceAccessor())

        then:
        changeLog.getLogicalFilePath() == "liquibase/parser-logical/xml/logicalPathChangeLog.xml"
        changeLog.getPhysicalFilePath() == physicalPath

        changeLog.getPreconditions().getNestedPreconditions().size() == 0
        changeLog.getChangeSets().size() == 1
        changeLog.getChangeSets()[0].getFilePath() == "liquibase/parser-logical/xml/logicalPathChangeLog.xml"

    }

    def "changelog with preconditions can be parsed: preconditionsChangeLog.xml"() throws Exception {
        when:
        def path = "liquibase/parser/core/xml/preconditionsChangeLog.xml"
        def changeLog = new XMLChangeLogSAXParser().parse(path, new ChangeLogParameters(), new JUnitResourceAccessor());

        then:
        changeLog.getLogicalFilePath() == path
        changeLog.getPhysicalFilePath() == path

        changeLog.getPreconditions() != null
        changeLog.getPreconditions().getNestedPreconditions().size() == 2

        changeLog.getPreconditions().getNestedPreconditions()[0].getName() == "runningAs"
        ((RunningAsPrecondition) changeLog.getPreconditions().getNestedPreconditions()[0]).getUsername() == "testUser"

        changeLog.getPreconditions().getNestedPreconditions().get(1).getName() == "or"
        ((OrPrecondition) changeLog.getPreconditions().getNestedPreconditions().get(1)).getNestedPreconditions()[0].getName() == "dbms"
        ((DBMSPrecondition) ((OrPrecondition) changeLog.getPreconditions().getNestedPreconditions().get(1)).getNestedPreconditions()[0]).getType() == "mssql"
        ((OrPrecondition) changeLog.getPreconditions().getNestedPreconditions().get(1)).getNestedPreconditions().get(1).getName() == "dbms"
        ((DBMSPrecondition) ((OrPrecondition) changeLog.getPreconditions().getNestedPreconditions().get(1)).getNestedPreconditions().get(1)).getType() == "mysql"

        changeLog.getChangeSets().size() == 1
    }

    @Unroll("#featureName #path")
    def "changeSets with one level of includes parse correctly"() throws Exception {
        when:
        DatabaseChangeLog changeLog = new XMLChangeLogSAXParser().parse(path, new ChangeLogParameters(), new JUnitResourceAccessor())

        then:
        changeLog.getLogicalFilePath() == path
        changeLog.getPhysicalFilePath() == path

        ((PreconditionContainer) changeLog.getPreconditions().getNestedPreconditions()[0]).getNestedPreconditions().size() == 0
        changeLog.getChangeSets().size() == 7
        changeLog.getChangeSets()[0].toString(false) == "${path}::1::nvoxland"
        changeLog.getChangeSets()[1].toString(false) == "liquibase/parser/core/xml/simpleChangeLog.xml::1::nvoxland"
        changeLog.getChangeSets()[2].toString(false) == "${path}::2::nvoxland"
        changeLog.getChangeSets()[3].toString(false) == "liquibase/parser/core/xml/included/included.changelog1.xml::1::nvoxland"
        changeLog.getChangeSets()[4].toString(false) == "liquibase/parser/core/xml/included/included.changelog2.xml::1::nvoxland"
        changeLog.getChangeSets()[5].toString(false) == "liquibase/parser/core/xml/included/raw-2.sql::raw::includeAll"
        changeLog.getChangeSets()[6].toString(false) == "liquibase/parser/core/xml/included/raw.sql::raw::includeAll"

        ((CreateTableChange) changeLog.getChangeSets()[0].getChanges()[0]).getTableName() == "employee"
        ((CreateTableChange) changeLog.getChangeSet("liquibase/parser/core/xml/simpleChangeLog.xml", "nvoxland", "1").getChanges()[0]).getTableName() == "person"
        ((AddColumnChange) changeLog.getChangeSet(path, "nvoxland", "2").getChanges()[0]).getTableName() == "employee"
        ((CreateTableChange) changeLog.getChangeSet("liquibase/parser/core/xml/included/included.changelog1.xml", "nvoxland", "1").getChanges()[0]).getTableName() == "included_table_1"
        ((CreateTableChange) changeLog.getChangeSet("liquibase/parser/core/xml/included/included.changelog2.xml", "nvoxland", "1").getChanges()[0]).getTableName() == "included_table_2"

        where:
        path << ["liquibase/parser/core/xml/nestedChangeLog.xml", "liquibase/parser/core/xml/nestedRelativeChangeLog.xml"]
    }

    @Unroll("#featureName #doubleNestedFileName")
    def "changeSets with two levels of includes parse correctly"() throws Exception {
        when:
        DatabaseChangeLog changeLog = new XMLChangeLogSAXParser().parse(doubleNestedFileName, new ChangeLogParameters(), new JUnitResourceAccessor());

        then:
        changeLog.getLogicalFilePath() == doubleNestedFileName
        changeLog.getPhysicalFilePath() == doubleNestedFileName

        changeLog.getPreconditions().getNestedPreconditions().size() == 1
        PreconditionContainer nested = (PreconditionContainer) changeLog.getPreconditions().getNestedPreconditions()[0];
        ((PreconditionContainer) nested.getNestedPreconditions()[0]).getNestedPreconditions().size() == 0
        changeLog.getChangeSets().size() == 8
        changeLog.getChangeSets()[0].toString(false) == "${doubleNestedFileName}::1::nvoxland"
        changeLog.getChangeSets()[1].toString(false) == "${nestedFileName}::1::nvoxland"
        changeLog.getChangeSets()[2].toString(false) == "liquibase/parser/core/xml/simpleChangeLog.xml::1::nvoxland"
        changeLog.getChangeSets()[3].toString(false) == "${nestedFileName}::2::nvoxland"
        changeLog.getChangeSets()[4].toString(false) == "liquibase/parser/core/xml/included/included.changelog1.xml::1::nvoxland"
        changeLog.getChangeSets()[5].toString(false) == "liquibase/parser/core/xml/included/included.changelog2.xml::1::nvoxland"
        changeLog.getChangeSets()[6].toString(false) == "liquibase/parser/core/xml/included/raw-2.sql::raw::includeAll"
        changeLog.getChangeSets()[7].toString(false) == "liquibase/parser/core/xml/included/raw.sql::raw::includeAll"

        ((CreateTableChange) changeLog.getChangeSet(doubleNestedFileName, "nvoxland", "1").changes[0]).getTableName() == "partner"
        ((CreateTableChange) changeLog.getChangeSet(nestedFileName, "nvoxland", "1").changes[0]).getTableName() == "employee"
        ((CreateTableChange) changeLog.getChangeSet("liquibase/parser/core/xml/simpleChangeLog.xml", "nvoxland", "1").changes[0]).getTableName() == "person"
        ((AddColumnChange) changeLog.getChangeSet(nestedFileName, "nvoxland", "2").changes[0]).getTableName() == "employee"

        where:
        doubleNestedFileName | nestedFileName
        "liquibase/parser/core/xml/doubleNestedChangeLog.xml" | "liquibase/parser/core/xml/nestedChangeLog.xml"
        "liquibase/parser/core/xml/doubleNestedRelativeChangeLog.xml" | "liquibase/parser/core/xml/nestedRelativeChangeLog.xml"

    }

    def "ChangeLogParseException thrown if changelog does not exist"() throws Exception {
        when:
        def path = "liquibase/changelog/parser/xml/missingChangeLog.xml"
        new XMLChangeLogSAXParser().parse(path, new ChangeLogParameters(), new JUnitResourceAccessor())

        then:
        def e = thrown(ChangeLogParseException)
        e.message == "${path} does not exist"
    }

    def "ChangeLogParseException thrown if changelog has invalid tags"() throws Exception {
        when:
        new XMLChangeLogSAXParser().parse("liquibase/parser/core/xml/malformedChangeLog.xml", new ChangeLogParameters(), new JUnitResourceAccessor())

        then:
        def e = thrown(ChangeLogParseException)
        assert e.message.startsWith("Error parsing line")
    }

    def "ChangeLogParseException thrown if changelog is invalid XML: invalidChangeLog.xml"() throws Exception {
        when:
        new XMLChangeLogSAXParser().parse("liquibase/parser/core/xml/invalidChangeLog.xml", new ChangeLogParameters(), new JUnitResourceAccessor())

        then:
        def e = thrown(ChangeLogParseException)
        assert e.message.startsWith("Error parsing line")
    }

    @FailsWith(ChangeLogParseException.class)
    def "tags that don't correspond to anything in liquibase are ignored"() throws Exception {
        def path = "liquibase/parser/core/xml/unusedTagsChangeLog.xml"
        expect:
        DatabaseChangeLog changeLog = new XMLChangeLogSAXParser().parse(path, new ChangeLogParameters(), new JUnitResourceAccessor());

        changeLog.getLogicalFilePath() == path
        changeLog.getPhysicalFilePath() == path

        changeLog.getPreconditions().getNestedPreconditions().size() == 0
        changeLog.getChangeSets().size() == 1

        ChangeSet changeSet = changeLog.getChangeSets()[0];
        changeSet.getAuthor() == "nvoxland"
        changeSet.getId() == "1"
        changeSet.getChanges().size() == 1
        changeSet.getFilePath() == path
        changeSet.getComments() == "Some comments go here"

        Change change = changeSet.getChanges()[0];
        ChangeFactory.getInstance().getChangeMetaData(change).getName() == "createTable"
        assert change instanceof CreateTableChange
    }

	def "changeLog parameters are correctly expanded"() throws Exception {
        when:
        def params = new ChangeLogParameters(new MockDatabase());
        params.setContexts(new Contexts("prod"))
		params.set("tablename", "my_table_name");
        params.set("tablename2", "my_table_name_2");
        params.set("columnName", "my_column_name");
        params.set("date", new Date(9999999));
        params.set("overridden", "Value passed in")
		def changeLog = new XMLChangeLogSAXParser().parse("liquibase/parser/core/xml/parametersChangeLog.xml", params, new JUnitResourceAccessor());

        then: "changeSet 1"
		changeLog.getChangeSets().size() == 2

		changeLog.getChangeSets()[0].getAuthor() == "paikens"
		changeLog.getChangeSets()[0].getId() == "1"
        changeLog.getChangeSets()[0].comments == "Some values: overridden: 'Value passed in', not.overridden: 'value from changelog 2', database: 'database mock', contextNote: 'context prod', contextNote2: '\${contextNote2}'"
		((RawSQLChange) changeLog.getChangeSets()[0].getChanges()[0]).getSql() == "create table my_table_name;"
		((RawSQLChange) changeLog.getChangeSets()[0].getRollBackChanges()[0]).getSql() == "drop table my_table_name"

        and: "changeSet 2"
        changeLog.getChangeSets().get(1).getAuthor() == "nvoxland"
        changeLog.getChangeSets().get(1).getId() == "2"
        changeLog.getChangeSets().get(1).comments == "Some values from the file: fileProperty1: 'property1 from file', fileProperty2: 'property2 from file'"

        ((CreateTableChange) changeLog.getChangeSets().get(1).getChanges()[0]).getTableName() == "my_table_name_2"
        ((CreateTableChange) changeLog.getChangeSets().get(1).getChanges()[0]).getColumns()[0].getName() == "my_column_name"
        ((CreateTableChange) changeLog.getChangeSets().get(1).getChanges()[0]).getColumns()[0].getDefaultValue() == "a string with an \${unused} param against database mock"

	}

	def "tests for particular features and edge conditions part 1 testCasesChangeLog.xml"() throws Exception {
        when:
        def path = "liquibase/parser/core/xml/testCasesChangeLog.xml"
        DatabaseChangeLog changeLog = new XMLChangeLogSAXParser().parse(path, new ChangeLogParameters(), new JUnitResourceAccessor());

        then: "before/after/position attributes are read correctly"
        ((AddColumnChange) changeLog.getChangeSet(path, "cmouttet", "using after column attribute").changes[0]).columns[0].getName() == "middlename";
        ((AddColumnChange) changeLog.getChangeSet(path, "cmouttet", "using after column attribute").changes[0]).columns[0].getAfterColumn() == "firstname";

        ((AddColumnChange) changeLog.getChangeSet(path, "cmouttet", "using before column attribute").changes[0]).columns[0].getName() == "middlename";
        ((AddColumnChange) changeLog.getChangeSet(path, "cmouttet", "using before column attribute").changes[0]).columns[0].getBeforeColumn() == "lastname";

        ((AddColumnChange) changeLog.getChangeSet(path, "cmouttet", "using position attribute").changes[0]).columns[0].getName() == "middlename";
        ((AddColumnChange) changeLog.getChangeSet(path, "cmouttet", "using position attribute").changes[0]).columns[0].getPosition() == 1;

        and: "validCheckSums are parsed"
        that changeLog.getChangeSet(path, "nvoxland", "validCheckSums set").getValidCheckSums(), containsInAnyOrder([CheckSum.parse("a9b7b29ce3a75940858cd022501852e2"), CheckSum.parse("8:b3d6a29ce3a75940858cd093501151d1")].toArray())
        that changeLog.getChangeSet(path, "nvoxland", "validCheckSums any").getValidCheckSums(), containsInAnyOrder([CheckSum.parse("ANY")].toArray())

        and: "changeSet with only preconditions is parsed correctly"
        changeLog.getChangeSet(path, "nvoxland", "only preconditions").changes.size() == 0
        changeLog.getChangeSet(path, "nvoxland", "only preconditions").preconditions.nestedPreconditions[0].name == "sqlCheck"

        and: "changeSet with multiple changes is parsed correctly"
        changeLog.getChangeSet(path, "nvoxland", "multiple changes").changes.size() == 4
        ((InsertDataChange) changeLog.getChangeSet(path, "nvoxland", "multiple changes").changes[0]).columns[0].valueNumeric == 1
        ((InsertDataChange) changeLog.getChangeSet(path, "nvoxland", "multiple changes").changes[1]).columns[0].valueNumeric == 2
        ((UpdateDataChange) changeLog.getChangeSet(path, "nvoxland", "multiple changes").changes[2]).columns[0].valueNumeric == 3
        ((InsertDataChange) changeLog.getChangeSet(path, "nvoxland", "multiple changes").changes[3]).columns[0].valueNumeric == 4

        and: "changeSet level attributes are parsed correctly"
        that changeLog.getChangeSet(path, "nvoxland", "context and dbms").contexts.contexts, containsInAnyOrder(["test", "qa"].toArray())
        that changeLog.getChangeSet(path, "nvoxland", "context and dbms").dbmsSet, containsInAnyOrder(["mock", "oracle"].toArray())
        changeLog.getChangeSet(path, "nvoxland", "standard changeSet").contexts.contexts.size() == 0
        changeLog.getChangeSet(path, "nvoxland", "standard changeSet").dbmsSet == null

        assert changeLog.getChangeSet(path, "nvoxland", "runAlways set").isAlwaysRun()
        assert !changeLog.getChangeSet(path, "nvoxland", "standard changeSet").isAlwaysRun()

        assert changeLog.getChangeSet(path, "nvoxland", "runOnChange set").isRunOnChange()
        assert !changeLog.getChangeSet(path, "nvoxland", "standard changeSet").isRunOnChange()

        assert !changeLog.getChangeSet(path, "nvoxland", "failOnError set").getFailOnError()
        assert changeLog.getChangeSet(path, "nvoxland", "standard changeSet").getFailOnError() == null

        changeLog.getChangeSet("com/example/other/path.xml", "nvoxland", "logicalFilePath set").getFilePath() == "com/example/other/path.xml"
        changeLog.getChangeSet(path, "nvoxland", "standard changeSet").getFilePath() == path

        changeLog.getChangeSet(path, "nvoxland", "objectQuotingStrategy LEGACY").getObjectQuotingStrategy() == ObjectQuotingStrategy.LEGACY
        changeLog.getChangeSet(path, "nvoxland", "objectQuotingStrategy ALL").getObjectQuotingStrategy() == ObjectQuotingStrategy.QUOTE_ALL_OBJECTS
        changeLog.getChangeSet(path, "nvoxland", "objectQuotingStrategy RESERVED").getObjectQuotingStrategy() == ObjectQuotingStrategy.QUOTE_ONLY_RESERVED_WORDS
        changeLog.getChangeSet(path, "nvoxland", "standard changeSet").getObjectQuotingStrategy() == ObjectQuotingStrategy.LEGACY

        changeLog.getChangeSet(path, "nvoxland", "onValidationFail HALT").onValidationFail == ChangeSet.ValidationFailOption.HALT
        changeLog.getChangeSet(path, "nvoxland", "onValidationFail MARK_RAN").onValidationFail == ChangeSet.ValidationFailOption.MARK_RAN
        changeLog.getChangeSet(path, "nvoxland", "standard changeSet").onValidationFail == ChangeSet.ValidationFailOption.HALT

        assert !changeLog.getChangeSet(path, "nvoxland", "runInTransaction set").runInTransaction
        assert changeLog.getChangeSet(path, "nvoxland", "standard changeSet").runInTransaction
    }

    def "tests for particular features and edge conditions part 2 testCasesChangeLog.xml"() throws Exception {
        when:
        def path = "liquibase/parser/core/xml/testCasesChangeLog.xml"
        DatabaseChangeLog changeLog = new XMLChangeLogSAXParser().parse(path, new ChangeLogParameters(), new JUnitResourceAccessor());


        then: "comment in sql is parsed correctly"
        changeLog.getChangeSet(path, "nvoxland", "comment in sql").comments == "This is a changeSet level comment"
        ((RawSQLChange) changeLog.getChangeSet(path, "nvoxland", "comment in sql").changes[0]).comment == "There is a comment in the SQL"
        ((RawSQLChange) changeLog.getChangeSet(path, "nvoxland", "comment in sql").changes[0]).sql == "select * from comment_in_sql"

        and: "column and constraints are parsed correctly"
        ((CreateTableChange) changeLog.getChangeSet(path, "nvoxland", "nested column and constraint objects").changes[0]).columns[0] != null
        ((CreateTableChange) changeLog.getChangeSet(path, "nvoxland", "nested column and constraint objects").changes[0]).columns[0].name == "id"
        ((CreateTableChange) changeLog.getChangeSet(path, "nvoxland", "nested column and constraint objects").changes[0]).columns[0].constraints != null
        ((CreateTableChange) changeLog.getChangeSet(path, "nvoxland", "nested column and constraint objects").changes[0]).columns[0].constraints.primaryKeyName == "pk_name"

        and: "precondition attributes are parsed correctly"
        changeLog.getChangeSet(path, "nvoxland", "precondition attributes 1").preconditions.onSqlOutput == PreconditionContainer.OnSqlOutputOption.FAIL
        changeLog.getChangeSet(path, "nvoxland", "precondition attributes 1").preconditions.onErrorMessage == "My Error Message"
        changeLog.getChangeSet(path, "nvoxland", "precondition attributes 1").preconditions.onFailMessage == "My Fail Message"
        changeLog.getChangeSet(path, "nvoxland", "precondition attributes 1").preconditions.onError == PreconditionContainer.ErrorOption.HALT
        changeLog.getChangeSet(path, "nvoxland", "precondition attributes 1").preconditions.onFail == PreconditionContainer.FailOption.HALT

        changeLog.getChangeSet(path, "nvoxland", "precondition attributes 2").preconditions.onSqlOutput == PreconditionContainer.OnSqlOutputOption.IGNORE
        changeLog.getChangeSet(path, "nvoxland", "precondition attributes 2").preconditions.onErrorMessage == null
        changeLog.getChangeSet(path, "nvoxland", "precondition attributes 2").preconditions.onFailMessage == null
        changeLog.getChangeSet(path, "nvoxland", "precondition attributes 2").preconditions.onError == PreconditionContainer.ErrorOption.CONTINUE
        changeLog.getChangeSet(path, "nvoxland", "precondition attributes 2").preconditions.onFail == PreconditionContainer.FailOption.CONTINUE

        changeLog.getChangeSet(path, "nvoxland", "precondition attributes 3").preconditions.onSqlOutput == PreconditionContainer.OnSqlOutputOption.TEST
        changeLog.getChangeSet(path, "nvoxland", "precondition attributes 3").preconditions.onError == PreconditionContainer.ErrorOption.MARK_RAN
        changeLog.getChangeSet(path, "nvoxland", "precondition attributes 3").preconditions.onFail == PreconditionContainer.FailOption.MARK_RAN

        changeLog.getChangeSet(path, "nvoxland", "precondition attributes 4").preconditions.onSqlOutput == PreconditionContainer.OnSqlOutputOption.IGNORE
        changeLog.getChangeSet(path, "nvoxland", "precondition attributes 4").preconditions.onError == PreconditionContainer.ErrorOption.WARN
        changeLog.getChangeSet(path, "nvoxland", "precondition attributes 4").preconditions.onFail == PreconditionContainer.FailOption.WARN

        and: "modifySql is parsed correctly"
        changeLog.getChangeSet(path, "nvoxland", "standard changeSet").sqlVisitors.size() == 0
        changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors.size() == 5
        ((ReplaceSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[0]).replace == "with_modifysql"
        ((ReplaceSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[0]).with == "after_modifysql"
        ((ReplaceSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[0]).applicableDbms == null
        ((ReplaceSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[0]).contexts == null
        assert !((ReplaceSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[0]).applyToRollback

        ((ReplaceSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[1]).replace == ")"
        ((ReplaceSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[1]).with == ""
        that(((ReplaceSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[1]).getApplicableDbms(), containsInAnyOrder(["mysql", "mock"].toArray()))
        ((ReplaceSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[1]).contexts == null

        ((AppendSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[2]).value == ", name varchar(255) )"
        ((AppendSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[2]).contexts == null
        that(((AppendSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[2]).applicableDbms, containsInAnyOrder(["mysql", "mock"].toArray()))
        assert ((AppendSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[2]).applyToRollback

        ((AppendSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[3]).value == " partitioned by stuff"
        ((AppendSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[3]).contexts.toString() == "(prod), (qa)"
        ((AppendSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[3]).applicableDbms == null

        ((AppendSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[4]).value == " engine innodb"
        ((AppendSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[4]).contexts.toString() == "(prod)"
        that(((AppendSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[4]).getApplicableDbms(), containsInAnyOrder(["mysql"].toArray()))

        and: "utf8 is read correctly"
        ((RawSQLChange) changeLog.getChangeSet(path, "nvoxland", "changeSet with UTF8").changes[0]).sql == "insert into testutf8insert (stringvalue) values ('string with € and £')"

        and: "rollback blocks are parsed correctly"
        changeLog.getChangeSet(path, "nvoxland", "standard changeSet").rollBackChanges.size() == 0

        changeLog.getChangeSet(path, "nvoxland", "one rollback block").rollBackChanges.length == 1
        ((RawSQLChange) changeLog.getChangeSet(path, "nvoxland", "one rollback block").rollBackChanges[0]).sql == "drop table rollback_test"

        changeLog.getChangeSet(path, "nvoxland", "empty rollback block").rollBackChanges.size() == 1
        assert changeLog.getChangeSet(path, "nvoxland", "empty rollback block").rollBackChanges[0] instanceof EmptyChange


        changeLog.getChangeSet(path, "nvoxland", "multiple rollback blocks").rollBackChanges.length == 7
        ((RawSQLChange) changeLog.getChangeSet(path, "nvoxland", "multiple rollback blocks").rollBackChanges[0]).sql == "drop table multiRollback1"
        ((RawSQLChange) changeLog.getChangeSet(path, "nvoxland", "multiple rollback blocks").rollBackChanges[1]).sql == "drop table multiRollback2"
        ((RawSQLChange) changeLog.getChangeSet(path, "nvoxland", "multiple rollback blocks").rollBackChanges[2]).sql == "drop table multiRollback3"
        ((DropTableChange) changeLog.getChangeSet(path, "nvoxland", "multiple rollback blocks").rollBackChanges[3]).tableName == "multiRollback4"
        ((DropTableChange) changeLog.getChangeSet(path, "nvoxland", "multiple rollback blocks").rollBackChanges[4]).tableName == "multiRollback5"
        ((DropTableChange) changeLog.getChangeSet(path, "nvoxland", "multiple rollback blocks").rollBackChanges[5]).tableName == "multiRollback6"
        ((RawSQLChange) changeLog.getChangeSet(path, "nvoxland", "multiple rollback blocks").rollBackChanges[6]).sql == "select * from simple"

    }
    def "tests for particular features and edge conditions part 3 testCasesChangeLog.xml"() throws Exception {
        when:
        def path = "liquibase/parser/core/xml/testCasesChangeLog.xml"
        DatabaseChangeLog changeLog = new XMLChangeLogSAXParser().parse(path, new ChangeLogParameters(), new JUnitResourceAccessor());


        then: "complex preconditions are parsed"
        changeLog.getChangeSet(path, "nvoxland", "complex preconditions").preconditions.nestedPreconditions.size() == 2
        ((DBMSPrecondition) ((NotPrecondition) ((AndPrecondition) ((OrPrecondition) changeLog.getChangeSet(path, "nvoxland", "complex preconditions").preconditions.nestedPreconditions[0]).nestedPreconditions[0]).nestedPreconditions[0]).nestedPreconditions[0]).type == "oracle"
        ((DBMSPrecondition) ((NotPrecondition) ((AndPrecondition) ((OrPrecondition) changeLog.getChangeSet(path, "nvoxland", "complex preconditions").preconditions.nestedPreconditions[0]).nestedPreconditions[0]).nestedPreconditions[0]).nestedPreconditions[1]).type == "mysql"
        ((RunningAsPrecondition) ((AndPrecondition) ((OrPrecondition) changeLog.getChangeSet(path, "nvoxland", "complex preconditions").preconditions.nestedPreconditions[0]).nestedPreconditions[0]).nestedPreconditions[1]).username == "sa"

        ((NotPrecondition) ((OrPrecondition) changeLog.getChangeSet(path, "nvoxland", "complex preconditions").preconditions.nestedPreconditions[0]).nestedPreconditions[1]).nestedPreconditions.size() == 2

        ((PrimaryKeyExistsPrecondition) ((AndPrecondition) changeLog.getChangeSet(path, "nvoxland", "complex preconditions").preconditions.nestedPreconditions[1]).nestedPreconditions[1]).primaryKeyName == "test_pk"

        and: "custom change and preconditions are parsed"
        changeLog.getChangeSet(path, "nvoxland", "custom precondition and change").preconditions.nestedPreconditions.size() == 1
        ((CustomPreconditionWrapper) changeLog.getChangeSet(path, "nvoxland", "custom precondition and change").preconditions.nestedPreconditions[0]).getParamValue("name") == "test_1"
        ((CustomPreconditionWrapper) changeLog.getChangeSet(path, "nvoxland", "custom precondition and change").preconditions.nestedPreconditions[0]).getParamValue("count") == "31"
        changeLog.getChangeSet(path, "nvoxland", "custom precondition and change").changes.size() == 1
        ((CustomChangeWrapper) changeLog.getChangeSet(path, "nvoxland", "custom precondition and change").changes[0]).getParamValue("tableName") == "tab_name"
        ((CustomChangeWrapper) changeLog.getChangeSet(path, "nvoxland", "custom precondition and change").changes[0]).getParamValue("columnName") == "col_name"
        ((CustomChangeWrapper) changeLog.getChangeSet(path, "nvoxland", "custom precondition and change").changes[0]).getParamValue("newValue") == ""

        and: "column nodes are parsed correctly"
        ((CreateTableChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[0]).columns[0].name == "id"
        ((CreateTableChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[0]).columns[0].type == "int"
        assert ((CreateTableChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[0]).columns[0].constraints.isPrimaryKey()
        assert !((CreateTableChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[0]).columns[0].constraints.isNullable()
        ((CreateTableChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[0]).columns[0].constraints.primaryKeyName == "pk_name"

        ((AddColumnChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[1]).columns[0].name == "new_col"
        ((AddColumnChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[1]).columns[0].type == "varchar(10)"
        ((AddColumnChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[1]).columns[0].defaultValue == "new value"
        assert !((AddColumnChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[1]).columns[0].constraints.isNullable()

        ((AddColumnChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[1]).columns[1].name == "new_col_int"
        ((AddColumnChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[1]).columns[1].defaultValueNumeric == 12

        ((AddColumnChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[1]).columns[2].name == "new_col_bool"
        assert ((AddColumnChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[1]).columns[2].defaultValueBoolean

        ((AddColumnChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[1]).columns[3].name == "new_col_computed"
        ((AddColumnChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[1]).columns[3].defaultValueComputed.toString() == "average_size()"

        ((AddColumnChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[1]).columns[4].name == "new_col_datetime"
        ((AddColumnChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[1]).columns[4].defaultValueDate.toString() == "2014-12-01 13:15:33.0"

        ((AddColumnChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[1]).columns[5].name == "new_col_seq"
        ((AddColumnChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[1]).columns[5].defaultValueSequenceNext.toString() == "seq_test"

        ((CreateIndexChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[2]).columns[0].name == "id"
        assert ((CreateIndexChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[2]).columns[0].constraints.isUnique()

        ((LoadDataChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[3]).columns[0].name == "id"
        ((LoadDataChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[3]).columns[1].name == "new_col"
        ((LoadDataChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[3]).columns[1].header == "new_col_header"

        ((UpdateDataChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[4]).columns[0].name == "new_col_boolean"
        ((UpdateDataChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[4]).columns[0].value == "false"

        and: "forms of update parse correctly"
        ((UpdateDataChange) changeLog.getChangeSet(path, "nvoxland", "update with whereParams").changes[0]).tableName == "updateTest"
        ((UpdateDataChange) changeLog.getChangeSet(path, "nvoxland", "update with whereParams").changes[0]).where == "id=:value and other_val=:value"
        ((UpdateDataChange) changeLog.getChangeSet(path, "nvoxland", "update with whereParams").changes[0]).whereParams.size() == 2
        ((UpdateDataChange) changeLog.getChangeSet(path, "nvoxland", "update with whereParams").changes[0]).whereParams[0].valueNumeric == 134
        ((UpdateDataChange) changeLog.getChangeSet(path, "nvoxland", "update with whereParams").changes[0]).whereParams[1].name == "other_val"
        ((UpdateDataChange) changeLog.getChangeSet(path, "nvoxland", "update with whereParams").changes[0]).whereParams[1].valueNumeric == 768

        ((UpdateDataChange) changeLog.getChangeSet(path, "nvoxland", "update with whereParams").changes[1]).tableName == "updateTest"
        ((UpdateDataChange) changeLog.getChangeSet(path, "nvoxland", "update with whereParams").changes[1]).where == "id=2"
        ((UpdateDataChange) changeLog.getChangeSet(path, "nvoxland", "update with whereParams").changes[1]).whereParams.size() == 0

        and: "shell commmand parses correctly"
        ((ExecuteShellCommandChange) changeLog.getChangeSet(path, "nvoxland", "shell command").changes[0]).executable == "/usr/bin/test"
        ((ExecuteShellCommandChange) changeLog.getChangeSet(path, "nvoxland", "shell command").changes[0]).os == ["linux", "mac"]
        ((ExecuteShellCommandChange) changeLog.getChangeSet(path, "nvoxland", "shell command").changes[0]).args == ["-out", "-test"]

        and: "view change parsed correctly"
        ((CreateViewChange) changeLog.getChangeSet(path, "nvoxland", "view creation").changes[0]).viewName == "test_view"
        ((CreateViewChange) changeLog.getChangeSet(path, "nvoxland", "view creation").changes[0]).selectQuery == "select * from test_table"

        and: "stop change parsed correctly"
        ((StopChange) changeLog.getChangeSet(path, "nvoxland", "stop change").changes[0]).message == "Stop message!"

        and: "large numbers are parsed correctly"
        ((CreateSequenceChange) changeLog.getChangeSet(path, "nvoxland", "large number").changes[0]).maxValue.toString() ==  "9999999999999999999999999999"
    }

    def "changelog with multiple dropColumn columns can be parsed"() throws Exception {
        when:
        def path = "liquibase/parser/core/xml/addDropColumnsChangeLog.xml"
        def changeLog = new XMLChangeLogSAXParser().parse(path, new ChangeLogParameters(), new JUnitResourceAccessor());

        then:  "add columns"
        assert 2 == changeLog.getChangeSets().get(1).getChanges().get(0).getColumns().size()
        assert "firstname" == changeLog.getChangeSets().get(1).getChanges().get(0).getColumns().get(0).getName()
        assert "lastname" == changeLog.getChangeSets().get(1).getChanges().get(0).getColumns().get(1).getName()

        and: "multiple drop columns"
        assert 2 == changeLog.getChangeSets().get(2).getChanges().get(0).getColumns().size()
        assert "firstname" == changeLog.getChangeSets().get(2).getChanges().get(0).getColumns().get(0).getName()
        assert "lastname" == changeLog.getChangeSets().get(2).getChanges().get(0).getColumns().get(1).getName()

        and: "single drop column"
        assert 0 == changeLog.getChangeSets().get(3).getChanges().get(0).getColumns().size()
        assert "id" == changeLog.getChangeSets().get(3).getChanges().get(0).getColumnName()
    }
}
