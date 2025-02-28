package liquibase.parser.core.yaml

import com.example.liquibase.change.ColumnConfigExample
import com.example.liquibase.change.ComputedConfig
import com.example.liquibase.change.CreateTableExampleChange
import com.example.liquibase.change.DefaultConstraintConfig
import com.example.liquibase.change.IdentityConfig
import com.example.liquibase.change.KeyColumnConfig
import com.example.liquibase.change.PrimaryKeyConfig
import com.example.liquibase.change.UniqueConstraintConfig

import liquibase.Contexts
import liquibase.Scope
import liquibase.change.ChangeFactory
import liquibase.change.CheckSum
import liquibase.change.core.AddColumnChange
import liquibase.change.core.CreateIndexChange
import liquibase.change.core.CreateTableChange
import liquibase.change.core.CreateViewChange
import liquibase.change.core.DropTableChange
import liquibase.change.core.EmptyChange
import liquibase.change.core.ExecuteShellCommandChange
import liquibase.change.core.InsertDataChange
import liquibase.change.core.LoadDataChange
import liquibase.change.core.RawSQLChange
import liquibase.change.core.StopChange
import liquibase.change.core.UpdateDataChange
import liquibase.change.custom.CustomChangeWrapper
import liquibase.change.custom.ExampleCustomSqlChange;
import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog
import liquibase.database.ObjectQuotingStrategy
import liquibase.database.core.MockDatabase;
import liquibase.exception.ChangeLogParseException
import liquibase.precondition.CustomPreconditionWrapper
import liquibase.precondition.core.AndPrecondition
import liquibase.precondition.core.DBMSPrecondition
import liquibase.precondition.core.NotPrecondition
import liquibase.precondition.core.OrPrecondition
import liquibase.precondition.core.PreconditionContainer
import liquibase.precondition.core.PrimaryKeyExistsPrecondition
import liquibase.precondition.core.RunningAsPrecondition
import liquibase.sdk.supplier.resource.ResourceSupplier
import liquibase.sql.visitor.AppendSqlVisitor
import liquibase.sql.visitor.ReplaceSqlVisitor
import liquibase.test.JUnitResourceAccessor
import liquibase.util.ISODateFormat
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static org.hamcrest.Matchers.containsInAnyOrder
import static spock.util.matcher.HamcrestSupport.that;

public class YamlChangeLogParser_RealFile_Test extends Specification {

    @Shared
            resourceSupplier = new ResourceSupplier()

    def "supports method identifies yaml files correctly"() {
        when:
        def parser = new YamlChangeLogParser()

        then:
        assert parser.supports("text.yaml", resourceSupplier.simpleResourceAccessor)
        assert parser.supports("text.YAML", resourceSupplier.simpleResourceAccessor)
        assert parser.supports("text.YaML", resourceSupplier.simpleResourceAccessor)
        assert parser.supports("com/example/text.yaml", resourceSupplier.simpleResourceAccessor)
        assert !parser.supports("com/example/text.xml", resourceSupplier.simpleResourceAccessor)
        assert !parser.supports("com/example/text.sql", resourceSupplier.simpleResourceAccessor)
        assert !parser.supports("com/example/text.unknown", resourceSupplier.simpleResourceAccessor)

    }

    def "able to parse a simple changelog simpleChangeLog.yaml"() throws ChangeLogParseException {
        def path = "liquibase/parser/core/yaml/simpleChangeLog.yaml"
        when:
        def changeLog = new YamlChangeLogParser().parse(path, new ChangeLogParameters(), new JUnitResourceAccessor());
        def changeSet = changeLog.changeSets[0]
        def change = changeSet.changes[0]

        then:
        changeLog.logicalFilePath == path
        changeLog.physicalFilePath == path

        changeLog.preconditions.nestedPreconditions.size() == 0
        changeLog.changeSets.size() == 1

        changeSet.author == "nvoxland"
        changeSet.id == "1"
        changeSet.changes.size() == 1
        changeSet.filePath == path
        changeSet.comments == "Some comments go here"

        assert change instanceof CreateTableChange
        change.tableName == "person_yaml"
        change.columns.size() == 3
        change.columns[0].name == "id"
        change.columns[0].type == "int"
        change.columns[0].constraints != null
        assert change.columns[0].constraints.isPrimaryKey()
        assert !change.columns[0].constraints.isNullable()

        change.columns[1].name == "name"
        change.columns[1].type == "varchar(255)"
        change.columns[1].constraints != null
        !change.columns[1].constraints.isNullable()


        change.columns[2].name == "age"
        change.columns[2].type == "int"
        change.columns[2].constraints == null
    }

    def "throws a nice validation error when changeSet node has typo in name"() throws ChangeLogParseException {
        def path = "liquibase/parser/core/yaml/typoChangeLog.yaml"
        when:
        new YamlChangeLogParser().parse(path, new ChangeLogParameters(), new JUnitResourceAccessor());

        then:
        thrown(ChangeLogParseException.class)
    }

    def "able to parse a changelog with multiple changeSets multiChangeSetChangeLog.yaml"() throws Exception {
        def path = "liquibase/parser/core/yaml/multiChangeSetChangeLog.yaml"
        when:
        DatabaseChangeLog changeLog = new YamlChangeLogParser().parse(path, new ChangeLogParameters(), new JUnitResourceAccessor());

        then:
        changeLog.getLogicalFilePath() == path
        changeLog.getPhysicalFilePath() == path

        changeLog.getPreconditions().getNestedPreconditions().size() == 0
        changeLog.getChangeSets().size() == 4

        changeLog.getChangeSet(path, "nvoxland", "1").changes.size() == 1
        changeLog.getChangeSet(path, "nvoxland", "1").getFilePath() == path
        changeLog.getChangeSet(path, "nvoxland", "1").getComments() == null
        assert !changeLog.getChangeSet(path, "nvoxland", "1").shouldAlwaysRun()
        assert !changeLog.getChangeSet(path, "nvoxland", "1").shouldRunOnChange()

        Scope.getCurrentScope().getSingleton(ChangeFactory.class).getChangeMetaData(changeLog.getChangeSets().get(0).getChanges().get(0)).getName() == "createTable"
        assert changeLog.getChangeSets().get(0).getChanges().get(0) instanceof CreateTableChange

        then:
        changeLog.getChangeSet(path, "nvoxland", "2").changes.size() == 2
        changeLog.getChangeSet(path, "nvoxland", "2").getFilePath() == path
        changeLog.getChangeSet(path, "nvoxland", "2").getComments() == "Testing add column"
        assert changeLog.getChangeSet(path, "nvoxland", "2").shouldAlwaysRun()
        assert changeLog.getChangeSet(path, "nvoxland", "2").shouldRunOnChange()
        changeLog.getChangeSet(path, "nvoxland", "2").rollback.changes.size() == 2
        assert changeLog.getChangeSet(path, "nvoxland", "2").rollback.changes[0] instanceof RawSQLChange
        assert changeLog.getChangeSet(path, "nvoxland", "2").rollback.changes[1] instanceof RawSQLChange

        Scope.getCurrentScope().getSingleton(ChangeFactory.class).getChangeMetaData(changeLog.getChangeSet(path, "nvoxland", "2").getChanges().get(0)).getName() == "addColumn"
        assert changeLog.getChangeSet(path, "nvoxland", "2").getChanges().get(0) instanceof AddColumnChange

        Scope.getCurrentScope().getSingleton(ChangeFactory.class).getChangeMetaData(changeLog.getChangeSet(path, "nvoxland", "2").getChanges().get(1)).getName() == "addColumn"
        assert changeLog.getChangeSets().get(1).getChanges().get(1) instanceof AddColumnChange

        changeLog.getChangeSet(path, "bob", "3").getChanges().size() == 1
        changeLog.getChangeSet(path, "bob", "3").getFilePath() == path
        changeLog.getChangeSet(path, "bob", "3").getComments() == null
        assert !changeLog.getChangeSet(path, "bob", "3").shouldAlwaysRun()
        assert !changeLog.getChangeSet(path, "bob", "3").shouldRunOnChange()

        Scope.getCurrentScope().getSingleton(ChangeFactory.class).getChangeMetaData(changeLog.getChangeSet(path, "bob", "3").getChanges().get(0)).getName() == "createTable"
        assert changeLog.getChangeSet(path, "bob", "3").getChanges().get(0) instanceof CreateTableChange


        changeLog.getChangeSets().get(3).getChanges().size() == 1

        assert changeLog.getChangeSets().get(3).getChanges().get(0) instanceof CustomChangeWrapper
        assert changeLog.getChangeSets().get(3).getChanges().get(0).getCustomChange() instanceof ExampleCustomSqlChange
        changeLog.getChangeSets().get(3).getChanges().get(0).generateStatements(new MockDatabase()) //fills out customChange params
        changeLog.getChangeSets().get(3).getChanges().get(0).getCustomChange().getTableName() == "table"
        changeLog.getChangeSets().get(3).getChanges().get(0).getCustomChange().getColumnName() == "column"
    }


    def "local path can be set in changelog file logicalPathChangeLog.yaml"() throws Exception {
        when:
        def physicalPath = "liquibase/parser/core/yaml/logicalPathChangeLog.yaml"
        def changeLog = new YamlChangeLogParser().parse(physicalPath, new ChangeLogParameters(), new JUnitResourceAccessor())

        then:
        changeLog.getLogicalFilePath() == "liquibase/parser-logical/yaml/logicalPathChangeLog.yaml"
        changeLog.getPhysicalFilePath() == physicalPath

        changeLog.getPreconditions().getNestedPreconditions().size() == 0
        changeLog.getChangeSets().size() == 1
        changeLog.getChangeSets().get(0).getFilePath() == "liquibase/parser-logical/yaml/logicalPathChangeLog.yaml"

    }

    def "changelog with preconditions can be parsed: preconditionsChangeLog.yaml"() throws Exception {
        when:
        def path = "liquibase/parser/core/yaml/preconditionsChangeLog.yaml"
        def changeLog = new YamlChangeLogParser().parse(path, new ChangeLogParameters(), new JUnitResourceAccessor());

        then:
        changeLog.getLogicalFilePath() == path
        changeLog.getPhysicalFilePath() == path

        changeLog.getPreconditions() != null
        changeLog.getPreconditions().getNestedPreconditions()[0].getNestedPreconditions().size() == 2

        changeLog.getPreconditions().getNestedPreconditions()[0].getNestedPreconditions().get(0).getName() == "runningAs"
        ((RunningAsPrecondition) changeLog.getPreconditions().getNestedPreconditions()[0].getNestedPreconditions().get(0)).getUsername() == "testUser"

        changeLog.getPreconditions().getNestedPreconditions()[0].getNestedPreconditions().get(1).getName() == "or"
        ((OrPrecondition) changeLog.getPreconditions().getNestedPreconditions()[0].getNestedPreconditions().get(1)).getNestedPreconditions().get(0).getName() == "dbms"
        ((DBMSPrecondition) ((OrPrecondition) changeLog.getPreconditions().getNestedPreconditions()[0].getNestedPreconditions().get(1)).getNestedPreconditions().get(0)).getType() == "mssql"
        ((OrPrecondition) changeLog.getPreconditions().getNestedPreconditions()[0].getNestedPreconditions().get(1)).getNestedPreconditions().get(1).getName() == "dbms"
        ((DBMSPrecondition) ((OrPrecondition) changeLog.getPreconditions().getNestedPreconditions()[0].getNestedPreconditions().get(1)).getNestedPreconditions().get(1)).getType() == "mysql"

        changeLog.getChangeSets().size() == 1
    }

    @Unroll("#featureName #path")
    def "changeSets with one level of includes parse correctly"() throws Exception {
        when:
        DatabaseChangeLog changeLog = new YamlChangeLogParser().parse(path, new ChangeLogParameters(), new JUnitResourceAccessor())

        then:
        changeLog.getLogicalFilePath() == path
        changeLog.getPhysicalFilePath() == path

        ((PreconditionContainer) changeLog.getPreconditions().getNestedPreconditions().get(0)).getNestedPreconditions().size() == 0
        changeLog.getChangeSets().size() == 7
        changeLog.getChangeSets()[0].toString(false) == "${path}::1::nvoxland"
        changeLog.getChangeSets()[1].toString(false) == "liquibase/parser/core/yaml/simpleChangeLog.yaml::1::nvoxland"
        changeLog.getChangeSets()[2].toString(false) == "${path}::2::nvoxland"
        changeLog.getChangeSets()[3].toString(false) == "liquibase/parser/core/yaml/included/included.changelog1.yaml::1::nvoxland"
        changeLog.getChangeSets()[4].toString(false) == "liquibase/parser/core/yaml/included/included.changelog2.yaml::1::nvoxland"
        changeLog.getChangeSets()[5].toString(false) == "liquibase/parser/core/yaml/included/raw-2.sql::raw::includeAll"
        changeLog.getChangeSets()[6].toString(false) == "liquibase/parser/core/yaml/included/raw.sql::raw::includeAll"

        ((CreateTableChange) changeLog.getChangeSets().get(0).getChanges().get(0)).getTableName() == "employee_yaml"
        ((CreateTableChange) changeLog.getChangeSet("liquibase/parser/core/yaml/simpleChangeLog.yaml", "nvoxland", "1").getChanges().get(0)).getTableName() == "person_yaml"
        ((AddColumnChange) changeLog.getChangeSet(path, "nvoxland", "2").getChanges().get(0)).getTableName() == "employee"
        ((CreateTableChange) changeLog.getChangeSet("liquibase/parser/core/yaml/included/included.changelog1.yaml", "nvoxland", "1").getChanges()[0]).getTableName() == "included_table_1"
        ((CreateTableChange) changeLog.getChangeSet("liquibase/parser/core/yaml/included/included.changelog2.yaml", "nvoxland", "1").getChanges()[0]).getTableName() == "included_table_2"

        where:
        path << ["liquibase/parser/core/yaml/nestedChangeLog.yaml", "liquibase/parser/core/yaml/nestedRelativeChangeLog.yaml"]
    }

    @Unroll("#featureName #doubleNestedFileName")
    def "changeSets with two levels of includes parse correctly"() throws Exception {
        when:
        DatabaseChangeLog changeLog = new YamlChangeLogParser().parse(doubleNestedFileName, new ChangeLogParameters(), new JUnitResourceAccessor());

        then:
        changeLog.getLogicalFilePath() == doubleNestedFileName
        changeLog.getPhysicalFilePath() == doubleNestedFileName

        changeLog.getPreconditions().getNestedPreconditions().size() == 1
        PreconditionContainer nested = (PreconditionContainer) changeLog.getPreconditions().getNestedPreconditions().get(0);
        ((PreconditionContainer) nested.getNestedPreconditions().get(0)).getNestedPreconditions().size() == 0
        changeLog.getChangeSets().size() == 8
        changeLog.getChangeSets()[0].toString(false) == "${doubleNestedFileName}::1::nvoxland"
        changeLog.getChangeSets()[1].toString(false) == "${nestedFileName}::1::nvoxland"
        changeLog.getChangeSets()[2].toString(false) == "liquibase/parser/core/yaml/simpleChangeLog.yaml::1::nvoxland"
        changeLog.getChangeSets()[3].toString(false) == "${nestedFileName}::2::nvoxland"
        changeLog.getChangeSets()[4].toString(false) == "liquibase/parser/core/yaml/included/included.changelog1.yaml::1::nvoxland"
        changeLog.getChangeSets()[5].toString(false) == "liquibase/parser/core/yaml/included/included.changelog2.yaml::1::nvoxland"
        changeLog.getChangeSets()[6].toString(false) == "liquibase/parser/core/yaml/included/raw-2.sql::raw::includeAll"
        changeLog.getChangeSets()[7].toString(false) == "liquibase/parser/core/yaml/included/raw.sql::raw::includeAll"

        ((CreateTableChange) changeLog.getChangeSet(doubleNestedFileName, "nvoxland", "1").changes[0]).getTableName() == "partner"
        ((CreateTableChange) changeLog.getChangeSet(nestedFileName, "nvoxland", "1").changes[0]).getTableName() == "employee_yaml"
        ((CreateTableChange) changeLog.getChangeSet("liquibase/parser/core/yaml/simpleChangeLog.yaml", "nvoxland", "1").changes[0]).getTableName() == "person_yaml"
        ((AddColumnChange) changeLog.getChangeSet(nestedFileName, "nvoxland", "2").changes[0]).getTableName() == "employee"

        where:
        doubleNestedFileName                                            | nestedFileName
        "liquibase/parser/core/yaml/doubleNestedChangeLog.yaml"         | "liquibase/parser/core/yaml/nestedChangeLog.yaml"
        "liquibase/parser/core/yaml/doubleNestedRelativeChangeLog.yaml" | "liquibase/parser/core/yaml/nestedRelativeChangeLog.yaml"
    }

    def "ChangeLogParseException thrown if changelog does not exist"() throws Exception {
        when:
        def path = "liquibase/changelog/parser/yaml/missingChangeLog.yaml"
        new YamlChangeLogParser().parse(path, new ChangeLogParameters(), new JUnitResourceAccessor())

        then:
        def e = thrown(ChangeLogParseException)
        e.message == "${path} does not exist"
    }

    def "ChangeLogParseException thrown if changelog has invalid tags"() throws Exception {
        when:
        new YamlChangeLogParser().parse("liquibase/parser/core/yaml/malformedChangeLog.yaml", new ChangeLogParameters(), new JUnitResourceAccessor())

        then:
        def e = thrown(ChangeLogParseException)
        assert e.message.startsWith("Syntax error in file liquibase/parser/core/yaml/malformedChangeLog.yaml")
    }

    @Ignore
    def "ChangeLogParseException thrown if changelog has two databaseChangeLog tags"() throws Exception {
        when:
        new YamlChangeLogParser().parse("liquibase/parser/core/yaml/malformedDoubleChangeLog.yaml", new ChangeLogParameters(), new JUnitResourceAccessor())

        then:
        def e = thrown(ChangeLogParseException)
        assert e.message.startsWith("Syntax error in file liquibase/parser/core/yaml/malformedDoubleChangeLog.yaml")
        assert e.message.contains("found duplicate key databaseChangeLog")
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
        def changeLog = new YamlChangeLogParser().parse("liquibase/parser/core/yaml/parametersChangeLog.yaml", params, new JUnitResourceAccessor());

        then: "changeSet 1"
        changeLog.getChangeSets().size() == 2

        changeLog.getChangeSets().get(0).getAuthor() == "paikens"
        changeLog.getChangeSets().get(0).getId() == "1"
        changeLog.getChangeSets().get(0).comments == "Some values: overridden: 'Value passed in', not.overridden: 'value from changelog 2', database: 'value from mock', contextNote: 'context prod', contextNote2: '\${contextNote2}'"
        ((RawSQLChange) changeLog.getChangeSets().get(0).getChanges().get(0)).getSql() == "create table my_table_name"
        ((RawSQLChange) changeLog.getChangeSets().get(0).rollback.changes[0]).getSql() == "drop table my_table_name"

        and: "changeSet 2"
        changeLog.getChangeSets().get(1).getAuthor() == "nvoxland"
        changeLog.getChangeSets().get(1).getId() == "2"
        changeLog.getChangeSets().get(1).comments == "Some values from the file: fileProperty1: 'property1 from file', fileProperty2: 'property2 from file'"

        ((CreateTableChange) changeLog.getChangeSets().get(1).getChanges()[0]).getTableName() == "my_table_name_2"
        ((CreateTableChange) changeLog.getChangeSets().get(1).getChanges()[0]).getColumns()[0].getName() == "my_column_name"
        ((CreateTableChange) changeLog.getChangeSets().get(1).getChanges()[0]).getColumns()[0].getDefaultValue() == "a string with an \${unused} param against value from mock"

    }


    def "tests for particular features and edge conditions part 1 testCasesChangeLog.yaml"() throws Exception {
        when:
        def path = "liquibase/parser/core/yaml/testCasesChangeLog.yaml"
        DatabaseChangeLog changeLog = new YamlChangeLogParser().parse(path, new ChangeLogParameters(), new JUnitResourceAccessor());

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
        that changeLog.getChangeSet(path, "nvoxland", "context and dbms").contextFilter.contexts, containsInAnyOrder(["test", "qa"].toArray())
        that changeLog.getChangeSet(path, "nvoxland", "context and dbms").dbmsSet, containsInAnyOrder(["mock", "oracle"].toArray())
        changeLog.getChangeSet(path, "nvoxland", "standard changeSet").contextFilter.contexts.size() == 0
        changeLog.getChangeSet(path, "nvoxland", "standard changeSet").dbmsSet == null

        assert changeLog.getChangeSet(path, "nvoxland", "runAlways set").isAlwaysRun()
        assert !changeLog.getChangeSet(path, "nvoxland", "standard changeSet").isAlwaysRun()

        assert changeLog.getChangeSet(path, "nvoxland", "runOnChange set").isRunOnChange()
        assert !changeLog.getChangeSet(path, "nvoxland", "standard changeSet").isRunOnChange()

        assert !changeLog.getChangeSet(path, "nvoxland", "failOnError set").getFailOnError()
        assert changeLog.getChangeSet(path, "nvoxland", "standard changeSet").getFailOnError() == null

        changeLog.getChangeSet("com/example/other/path.yaml", "nvoxland", "logicalFilePath set").getFilePath() == "com/example/other/path.yaml"
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

    def "tests for particular features and edge conditions part 2 testCasesChangeLog.yaml"() throws Exception {
        when:
        def path = "liquibase/parser/core/yaml/testCasesChangeLog.yaml"
        DatabaseChangeLog changeLog = new YamlChangeLogParser().parse(path, new ChangeLogParameters(), new JUnitResourceAccessor());


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
        ((ReplaceSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[0]).contextFilter == null
        assert !((ReplaceSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[0]).applyToRollback

        ((ReplaceSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[1]).replace == ")"
        ((ReplaceSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[1]).with == ""
        that(((ReplaceSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[1]).getApplicableDbms(), containsInAnyOrder(["mysql", "mock"].toArray()))
        ((ReplaceSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[1]).contextFilter == null

        ((AppendSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[2]).value == " , name varchar(255) )"
        ((AppendSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[2]).contextFilter == null
        that(((AppendSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[2]).applicableDbms, containsInAnyOrder(["mysql", "mock"].toArray()))
        assert ((AppendSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[2]).applyToRollback

        ((AppendSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[3]).value == " partitioned by stuff"
        ((AppendSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[3]).contextFilter.toString() == "prod, qa"
        ((AppendSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[3]).applicableDbms == null

        ((AppendSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[4]).value == " engine innodb"
        ((AppendSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[4]).contextFilter.toString() == "prod"
        that(((AppendSqlVisitor) changeLog.getChangeSet(path, "nvoxland", "changeSet with modifySql").sqlVisitors[4]).getApplicableDbms(), containsInAnyOrder(["mysql"].toArray()))

        and: "utf8 is read correctly"
        ((RawSQLChange) changeLog.getChangeSet(path, "nvoxland", "changeSet with UTF8").changes[0]).sql == "insert into testutf8insert (stringvalue) values ('string with € and £')"

        and: "rollback blocks are parsed correctly"
        changeLog.getChangeSet(path, "nvoxland", "standard changeSet").rollback.changes.size() == 0

        changeLog.getChangeSet(path, "nvoxland", "one rollback block").rollback.changes.size() == 1
        ((RawSQLChange) changeLog.getChangeSet(path, "nvoxland", "one rollback block").rollback.changes[0]).sql == "drop table rollback_test"

        changeLog.getChangeSet(path, "nvoxland", "empty rollback block").rollback.changes.size() == 1
        assert changeLog.getChangeSet(path, "nvoxland", "empty rollback block").rollback.changes[0] instanceof EmptyChange


        changeLog.getChangeSet(path, "nvoxland", "multiple rollback blocks").rollback.changes.size() == 6
        ((RawSQLChange) changeLog.getChangeSet(path, "nvoxland", "multiple rollback blocks").rollback.changes[0]).sql == "drop table multiRollback1"
        ((RawSQLChange) changeLog.getChangeSet(path, "nvoxland", "multiple rollback blocks").rollback.changes[1]).sql == "drop table multiRollback2"
        ((RawSQLChange) changeLog.getChangeSet(path, "nvoxland", "multiple rollback blocks").rollback.changes[2]).sql == "drop table multiRollback3"
        ((DropTableChange) changeLog.getChangeSet(path, "nvoxland", "multiple rollback blocks").rollback.changes[3]).tableName == "multiRollback4"
        ((DropTableChange) changeLog.getChangeSet(path, "nvoxland", "multiple rollback blocks").rollback.changes[4]).tableName == "multiRollback5"
        ((DropTableChange) changeLog.getChangeSet(path, "nvoxland", "multiple rollback blocks").rollback.changes[5]).tableName == "multiRollback6"

    }

    def "tests for particular features and edge conditions part 3 testCasesChangeLog.yaml"() throws Exception {
        when:
        def path = "liquibase/parser/core/yaml/testCasesChangeLog.yaml"
        DatabaseChangeLog changeLog = new YamlChangeLogParser().parse(path, new ChangeLogParameters(), new JUnitResourceAccessor());


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
        new ISODateFormat().format(new java.sql.Timestamp(((AddColumnChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[1]).columns[4].defaultValueDate.time)).matches(/2014-12-\d+T\d+:\d+:33/) //timezones shift actual value around

        ((AddColumnChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[1]).columns[5].name == "new_col_seq"
        ((AddColumnChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[1]).columns[5].defaultValueSequenceNext.toString() == "seq_test"

        ((CreateIndexChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[2]).columns[0].name == "id"
        assert ((CreateIndexChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[2]).columns[0].constraints.isUnique()

        ((LoadDataChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[3]).quotchar == "\""
        ((LoadDataChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[3]).columns[0].name == "id"
        ((LoadDataChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[3]).columns[1].name == "new_col"
        ((LoadDataChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[3]).columns[1].header == "new_col_header"

        ((UpdateDataChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[4]).columns[0].name == "new_col_boolean"
        ((UpdateDataChange) changeLog.getChangeSet(path, "nvoxland", "different object types for column").changes[4]).columns[0].value == "false"

        and: "forms of update parse correctly"
//        ((UpdateDataChange) changeLog.getChangeSet(path, "nvoxland", "update with whereParams").changes[0]).tableName == "updateTest"
//        ((UpdateDataChange) changeLog.getChangeSet(path, "nvoxland", "update with whereParams").changes[0]).where == "id=:value and other_val=:value"
//        ((UpdateDataChange) changeLog.getChangeSet(path, "nvoxland", "update with whereParams").changes[0]).whereParams.size() == 2
//        ((UpdateDataChange) changeLog.getChangeSet(path, "nvoxland", "update with whereParams").changes[0]).whereParams[0].valueNumeric == 134
//        ((UpdateDataChange) changeLog.getChangeSet(path, "nvoxland", "update with whereParams").changes[0]).whereParams[1].name == "other_val"
//        ((UpdateDataChange) changeLog.getChangeSet(path, "nvoxland", "update with whereParams").changes[0]).whereParams[1].valueNumeric == 768

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
    }

    def "nested objects are parsed"() {
        setup:
        Scope.getCurrentScope().getSingleton(ChangeFactory.class).register(new CreateTableExampleChange())

        when:
        def path = "liquibase/parser/core/yaml/nestedObjectsChangeLog.yaml"
        def changeLog = new YamlChangeLogParser().parse(path, new ChangeLogParameters(), new JUnitResourceAccessor());

        then:
        changeLog.getChangeSets().size() == 1
        changeLog.getChangeSets().get(0).getChanges().size() == 1
        def change1 = changeLog.getChangeSets().get(0).getChanges().get(0)
        changeLog.getChangeSets().get(0).getChanges().get(0).getClass() == CreateTableExampleChange
        change1.getClass() == CreateTableExampleChange
        change1.getSchemaName() == "dbo"
        change1.getTableName() == "Test"
        change1.getDecimalValue() == 3.14159
        change1.getColumns().size() == 7
        change1.getColumns().get(0).getClass() == ColumnConfigExample
        change1.getColumns().get(0).getName() == "id"
        change1.getColumns().get(0).getType() == "bigint"
        change1.getColumns().get(0).getNullable() == false
        change1.getColumns().get(0).getIdentity().getClass() == IdentityConfig
        change1.getColumns().get(0).getIdentity().getSeed() == 1
        change1.getColumns().get(0).getIdentity().getIncrement() == 1
        change1.getColumns().get(1).getClass() == ColumnConfigExample
        change1.getColumns().get(1).getName() == "key1"
        change1.getColumns().get(1).getType() == "nvarchar(40)"
        change1.getColumns().get(1).getNullable() == false
        change1.getColumns().get(1).getIdentity() == null
        change1.getColumns().get(1).getDefaultConstraint() == null
        change1.getColumns().get(1).getComputed() == null
        change1.getColumns().get(2).getClass() == ColumnConfigExample
        change1.getColumns().get(2).getName() == "key2"
        change1.getColumns().get(2).getType() == "nvarchar(20)"
        change1.getColumns().get(2).getNullable() == false
        change1.getColumns().get(3).getClass() == ColumnConfigExample
        change1.getColumns().get(3).getName() == "key3"
        change1.getColumns().get(3).getType() == "nvarchar(10)"
        change1.getColumns().get(3).getNullable() == true
        change1.getColumns().get(4).getClass() == ColumnConfigExample
        change1.getColumns().get(4).getName() == "value"
        change1.getColumns().get(4).getType() == "nvarchar(MAX)"
        change1.getColumns().get(4).getNullable() == false
        change1.getColumns().get(5).getClass() == ColumnConfigExample
        change1.getColumns().get(5).getName() == "lastUpdateDate"
        change1.getColumns().get(5).getType() == "datetime2"
        change1.getColumns().get(5).getNullable() == false
        change1.getColumns().get(5).getDefaultConstraint().getClass() == DefaultConstraintConfig
        change1.getColumns().get(5).getDefaultConstraint().getName() == "DF_Test_lastUpdateDate"
        change1.getColumns().get(5).getDefaultConstraint().getExpression() == "GETDATE()"
        change1.getColumns().get(6).getClass() == ColumnConfigExample
        change1.getColumns().get(6).getName() == "partition"
        change1.getColumns().get(6).getType() == null
        change1.getColumns().get(6).getNullable() == false
        change1.getColumns().get(6).getComputed().getClass() == ComputedConfig
        change1.getColumns().get(6).getComputed().getExpression() == "[id] % 5"
        change1.getColumns().get(6).getComputed().getPersisted() == true
        change1.getPrimaryKey().getClass() == PrimaryKeyConfig
        change1.getPrimaryKey().getName() == "PK_Test_id"
        change1.getPrimaryKey().getKeyColumns().size() == 1
        change1.getPrimaryKey().getKeyColumns().get(0).getClass() == KeyColumnConfig
        change1.getPrimaryKey().getKeyColumns().get(0).getName() == "id"
        change1.getUniqueConstraints().size() == 2
        change1.getUniqueConstraints().get(0).getClass() == UniqueConstraintConfig
        change1.getUniqueConstraints().get(0).getName() == "UQ_Test_key1"
        change1.getUniqueConstraints().get(0).getKeyColumns().size() == 1
        change1.getUniqueConstraints().get(0).getKeyColumns().get(0).getClass() == KeyColumnConfig
        change1.getUniqueConstraints().get(0).getKeyColumns().get(0).getName() == "key1"
        change1.getUniqueConstraints().get(1).getClass() == UniqueConstraintConfig
        change1.getUniqueConstraints().get(1).getName() == "UQ_Test_key2_key3_DESC"
        change1.getUniqueConstraints().get(1).getKeyColumns().size() == 2
        change1.getUniqueConstraints().get(1).getKeyColumns().get(0).getClass() == KeyColumnConfig
        change1.getUniqueConstraints().get(1).getKeyColumns().get(0).getName() == "key2"
        change1.getUniqueConstraints().get(1).getKeyColumns().get(0).getDescending() == null
        change1.getUniqueConstraints().get(1).getKeyColumns().get(1).getClass() == KeyColumnConfig
        change1.getUniqueConstraints().get(1).getKeyColumns().get(1).getName() == "key3"
        change1.getUniqueConstraints().get(1).getKeyColumns().get(1).getDescending() == true

        cleanup:
        Scope.getCurrentScope().getSingleton(ChangeFactory.class).unregister("createTableExample");
    }


    def "Verify Liquibase returns zero changesets when a YAML changelog only has the databaseChangeLog tag"() throws ChangeLogParseException {
        def path = "liquibase/parser/core/yaml/emptyChangeLog.yaml"
        when:
        def changeLog = new YamlChangeLogParser().parse(path, new ChangeLogParameters(), new JUnitResourceAccessor());

        then:
        changeLog.logicalFilePath == path
        changeLog.physicalFilePath == path

        changeLog.preconditions.nestedPreconditions.size() == 0
        changeLog.changeSets.size() == 0
    }
}
