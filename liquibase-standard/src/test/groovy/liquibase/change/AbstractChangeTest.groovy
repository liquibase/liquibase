package liquibase.change

import liquibase.ChecksumVersion
import liquibase.ContextExpression
import liquibase.Labels
import liquibase.Scope
import liquibase.change.core.CreateProcedureChange
import liquibase.change.core.SQLFileChange
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.database.Database
import liquibase.database.core.MSSQLDatabase
import liquibase.exception.RollbackImpossibleException
import liquibase.exception.UnexpectedLiquibaseException
import liquibase.exception.ValidationErrors
import liquibase.database.core.MockDatabase
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration
import liquibase.serializer.LiquibaseSerializable
import liquibase.statement.SqlStatement
import liquibase.util.TestUtil
import org.junit.Test
import spock.lang.Specification
import spock.lang.Unroll

import static liquibase.test.Assert.assertArraysEqual
import static org.junit.Assert.assertSame
import static org.junit.Assert.assertTrue

class AbstractChangeTest extends Specification {

    @Unroll("#featureName: #object")
    def "implementations of abstract change must not have any DatabaseChangeProperty annotations which duplicate versions"() {
        def methods = object.getMethods()
        expect:
        for (def method : methods) {
            Set<ChecksumVersion> seenVersions = new HashSet<>()
            def annotations = method.getAnnotationsByType(DatabaseChangeProperty.class)
            for (def annotation : annotations) {
                def versions = annotation.version()
                if (versions.size() == 0) {
                    assert !seenVersions.contains(null)
                    seenVersions.add(null)
                }
                for (def version : versions) {
                    assert !seenVersions.contains(version)
                    seenVersions.add(version)
                }
            }
        }

        where:
        object << TestUtil.getClasses(AbstractChange)
    }

    def "createChangeMetaData with no annotations"() {
        when:
        new AbstractChange() {
            @Override
            String getConfirmationMessage() {
                return null
            }

            @Override
            SqlStatement[] generateStatements(Database database) {
                return null
            }

            @Override
            String getSerializedObjectNamespace() {
                return STANDARD_CHANGELOG_NAMESPACE
            }

        }.createChangeMetaData()
        then:
        UnexpectedLiquibaseException e = thrown()
        assert e.getMessage().startsWith("liquibase.exception.UnexpectedLiquibaseException: No @DatabaseChange annotation for ")
    }

    def createChangeMetaData() {
        when:
        def change = new ExampleAbstractChange()
        ChangeMetaData changeMetaData = change.createChangeMetaData()
        Map<String, ChangeParameterMetaData> parameters = changeMetaData.getParameters()

        then:
        changeMetaData.getName() == "exampleAbstractChange"
        changeMetaData.getDescription() == "Used for the AbstractChangeTest unit test"
        changeMetaData.getPriority() == 1
        changeMetaData.getAppliesTo() == null

        parameters.size() == 3
        ChangeParameterMetaData dbmsMetaData = parameters.get("dbms")
        ChangeParameterMetaData paramOneMetaData = parameters.get("paramOne")
        ChangeParameterMetaData paramTwoMetaData = parameters.get("paramTwo")
        ChangeParameterMetaData paramNoMetaData = parameters.get("paramNoMetadata")
        ChangeParameterMetaData paramNotIncludedMetaData = parameters.get("paramNotIncluded")
        ChangeParameterMetaData paramNoWriteMethodMetaData = parameters.get("paramNoWriteMethod")

        dbmsMetaData == null
        paramOneMetaData != null
        paramOneMetaData.getParameterName() == "paramOne"
        paramOneMetaData.getDisplayName() == "Param One"
        paramOneMetaData.getDataType() == "string"
        paramOneMetaData.getMustEqualExisting() == ""
        paramOneMetaData.getRequiredForDatabase().size() == 0

        paramTwoMetaData != null
        paramTwoMetaData.getParameterName() == "paramTwo"
        paramTwoMetaData.getDisplayName() == "Param Two"
        paramTwoMetaData.getDataType() == "integer"
        paramTwoMetaData.getMustEqualExisting() == "table"
        paramTwoMetaData.getRequiredForDatabase().size() == 2
        assertTrue(paramTwoMetaData.getRequiredForDatabase().contains("mysql"))
        assertTrue(paramTwoMetaData.getRequiredForDatabase().contains("mssql"))

        paramNoMetaData != null
        paramNotIncludedMetaData == null
        assert paramNoWriteMethodMetaData == null : "Properties with no write method should not be included"
    }

    def createChangeMetaData_noParams() {
        when:
        def change = new ExampleParamlessAbstractChange()
        ChangeMetaData changeMetaData = change.createChangeMetaData()
        Map<String, ChangeParameterMetaData> parameters = changeMetaData.getParameters()

        then:
        parameters.size() == 0
    }

    def createChangeParameterMetadata_invalidParamNameEmptyParams() throws Exception {
        when:
        new ExampleParamlessAbstractChange().createChangeParameterMetadata("paramOne")
        then:
        thrown(UnexpectedLiquibaseException)
    }

    def createChangeParameterMetadata_invalidParamName() throws Exception {
        when:
        new ExampleAbstractChange().createChangeParameterMetadata("paramInvalid") == null
        then:
        thrown(UnexpectedLiquibaseException)
    }

    def getChangeSet() {
        when:
        def change = new ExampleAbstractChange()
        ChangeSet changeSet = new ChangeSet(new DatabaseChangeLog())
        change.setChangeSet(changeSet)

        then:
        assertSame(changeSet, change.getChangeSet())

        when:
        change.setChangeSet(null)
        then:
        change.getChangeSet() == null
    }

    def generateStatementsVolatile_noStatements() throws Exception {
        expect:
        assert !new ExampleAbstractChange() {
            @Override
            SqlStatement[] generateStatements(Database database) {
                return null
            }
        }.generateStatementsVolatile(new MockDatabase())

        assert !new ExampleAbstractChange() {
            @Override
            SqlStatement[] generateStatements(Database database) {
                return new SqlStatement[0]
            }
        }.generateStatementsVolatile(new MockDatabase())
    }

    def generateRollbackStatementsVolatile_noStatements() throws Exception {
        expect:
        assert !new ExampleAbstractChange() {
            @Override
            SqlStatement[] generateStatements(Database database) {
                return null
            }
        }.generateRollbackStatementsVolatile(new MockDatabase())

        assert !new ExampleAbstractChange() {
            @Override
            SqlStatement[] generateStatements(Database database) {
                return new SqlStatement[0]
            }
        }.generateRollbackStatementsVolatile(new MockDatabase())
    }

    void supports_noStatements() throws Exception {
        expect:
        assert new ExampleAbstractChange() {
            @Override
            SqlStatement[] generateStatements(Database database) {
                return null
            }
        }.supports(new MockDatabase())

        assert new ExampleAbstractChange() {
            @Override
            SqlStatement[] generateStatements(Database database) {
                return new SqlStatement[0]
            }
        }.supports(new MockDatabase())
    }

    def warn_noStatements() throws Exception {
        expect:
        assert !new ExampleAbstractChange() {
            @Override
            SqlStatement[] generateStatements(Database database) {
                return null
            }
        }.warn(new MockDatabase()).hasWarnings()

        assert !new ExampleAbstractChange() {
            @Override
            SqlStatement[] generateStatements(Database database) {
                return new SqlStatement[0]
            }
        }.warn(new MockDatabase()).hasWarnings()
    }

    def validate_noStatements() throws Exception {
        expect:
        assert !new ExampleAbstractChange() {
            @Override
            SqlStatement[] generateStatements(Database database) {
                return null
            }
        }.validate(new MockDatabase()).hasErrors()

        assert !new ExampleAbstractChange() {
            @Override
            SqlStatement[] generateStatements(Database database) {
                return new SqlStatement[0]
            }
        }.validate(new MockDatabase()).hasErrors()
    }

    def validate_noParameters() throws Exception {
        expect:
        assert !new ExampleParamlessAbstractChange().validate(new MockDatabase()).hasErrors()
    }

    def validate_missingRequiredValue() throws Exception {
        when:
        def change = new ExampleAbstractChange()
        ValidationErrors errors = change.validate(new MSSQLDatabase())
        then:

        errors.getErrorMessages().size() == 1
        errors.getErrorMessages().get(0) == "paramTwo is required for exampleAbstractChange on mssql"
    }

    def validate_noErrors() throws Exception {
        when:
        def change = new ExampleAbstractChange()
        change.setParamTwo(3)

        then:
        ValidationErrors errors = change.validate(new MSSQLDatabase())
        assert !errors.hasErrors()
    }

    def generateRollbackStatements_nullCreateInverse() throws Exception, RollbackImpossibleException {
        when:
        Database database = new MockDatabase()
        new ExampleParamlessAbstractChange() {
            @Override
            protected Change[] createInverses() {
                return null
            }
        }.generateRollbackStatements(database)

        then:
        thrown(RollbackImpossibleException)
    }

    def generateRollbackStatements_emptyCreateInverse() throws Exception, RollbackImpossibleException {
        when:
        Database database = new MockDatabase()

        SqlStatement[] rollbackStatements = new ExampleParamlessAbstractChange() {
            @Override
            protected Change[] createInverses() {
                return EMPTY_CHANGE
            }
        }.generateRollbackStatements(database)

        then:
        rollbackStatements.length == 0
    }

    def createInverses() {
        expect:
        new ExampleAbstractChange().createInverses() == null
    }

    def getAffectedDatabaseObjects_noStatements() throws Exception {
        expect:
        new ExampleAbstractChange() {
            @Override
            SqlStatement[] generateStatements(Database database) {
                return null
            }
        }.getAffectedDatabaseObjects(new MockDatabase()).size() == 0

        new ExampleAbstractChange() {
            @Override
            SqlStatement[] generateStatements(Database database) {
                return new SqlStatement[0]
            }
        }.getAffectedDatabaseObjects(new MockDatabase()).size() == 0
    }

    def getSerializableFieldType() {
        expect:
        new ExampleAbstractChange().getSerializableFieldType("paramOne") == LiquibaseSerializable.SerializationType.NAMED_FIELD
        new ExampleAbstractChange().getSerializableFieldType("paramTwo") == LiquibaseSerializable.SerializationType.NESTED_OBJECT
    }

    def createSinceMetaData_nullAnnotation() {
        expect:
        new ExampleAbstractChange().createSinceMetaData("x", null) == null
    }

    def createDescriptionMetaData_nullAnnotation() {
        expect:
        new ExampleAbstractChange().createDescriptionMetaData("x", null) == null
    }

    def createSerializationTypeMetaData_nullAnnotation() {
        LiquibaseSerializable.SerializationType.NAMED_FIELD == new ExampleAbstractChange().createSerializationTypeMetaData("x", null)
    }

    def createMustEqualExistingMetaData_nullAnnotation() {
        expect:
        new ExampleAbstractChange().createMustEqualExistingMetaData("x", null) == null
    }

    def createExampleMetaData_nullAnnotation() {
        expect:
        new ExampleAbstractChange().createExampleValueMetaData("x", null) == null
    }

    def createRequiredDatabasesMetaData_nullAnnotation() {
        assertArraysEqual(["COMPUTE"].toArray(), new ExampleAbstractChange().createRequiredDatabasesMetaData("x", null))
    }

    @Test
    void createSupportedDatabasesMetaData_nullAnnotation() {
        assertArraysEqual(["COMPUTE"].toArray(), new ExampleAbstractChange().createSupportedDatabasesMetaData("x", null))
    }

    @Unroll("#featureName: #path is included as part of description for a #change")
    void pathIsIncludedAsPartOfChangeDescription() {
        when:
        change.setPath(path);

        then:
        change.getDescription().contains(path) == true

        where:
        path | change
        "This/is/a/test/change/path" | new SQLFileChange()
        "This/is/a/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/ver/long/test/change/path" | new CreateProcedureChange()
    }

    @Unroll
    def "context filter checksum generation - #version"(ChecksumVersion version, String originalChecksum, String updatedChecksum) {
        when:
        ChangeSet originalChange = new ChangeSet("testId", "testAuthor", false, false, "path/changelog", null, null, null)
        ChangeSet changeWithContext = originalChange

        CheckSum changeWithoutContextCheckSum = Scope.child(Collections.singletonMap(Scope.Attr.checksumVersion.toString(), version), { -> return originalChange.generateCheckSum(version) } as Scope.ScopedRunnerWithReturn)
        changeWithContext.setContextFilter(new ContextExpression("test"))
        CheckSum changeWithContextCheckSum = changeWithContext.generateCheckSum(version)

        then:
        changeWithoutContextCheckSum.toString() == originalChecksum
        changeWithContextCheckSum.toString() == updatedChecksum

        where:
        version | originalChecksum | updatedChecksum
        ChecksumVersion.V8 | "8:d41d8cd98f00b204e9800998ecf8427e" | "8:d41d8cd98f00b204e9800998ecf8427e"
        ChecksumVersion.latest() | "9:d41d8cd98f00b204e9800998ecf8427e" | "9:d41d8cd98f00b204e9800998ecf8427e"
    }

    @Unroll
    def "label checksum generation - #version"(ChecksumVersion version, String originalChecksum, String updatedChecksum) {
        when:
        ChangeSet originalChange = new ChangeSet("testId", "testAuthor", false, false, "path/changelog", null, null, null)
        ChangeSet changeWithLabel = originalChange

        CheckSum changeWithoutLabelCheckSum = Scope.child(Collections.singletonMap(Scope.Attr.checksumVersion.toString(), version), { -> return originalChange.generateCheckSum(version) } as Scope.ScopedRunnerWithReturn)
        changeWithLabel.setLabels(new Labels("test"))
        CheckSum changeWithLabelCheckSum = changeWithLabel.generateCheckSum(version)

        then:
        changeWithoutLabelCheckSum.toString() == originalChecksum
        changeWithLabelCheckSum.toString() == updatedChecksum

        where:
        version | originalChecksum | updatedChecksum
        ChecksumVersion.V8 | "8:d41d8cd98f00b204e9800998ecf8427e" | "8:d41d8cd98f00b204e9800998ecf8427e"
        ChecksumVersion.latest() | "9:d41d8cd98f00b204e9800998ecf8427e" | "9:d41d8cd98f00b204e9800998ecf8427e"
    }

    @Unroll
    def "dbms checksum generation - #version"(ChecksumVersion version, String originalChecksum, String updatedChecksum) {
        when:
        ChangeSet originalChange = new ChangeSet("testId", "testAuthor", false, false, "path/changelog", null, null, null)
        ChangeSet changeWithDbms = originalChange

        CheckSum changeWithoutDbmsCheckSum = Scope.child(Collections.singletonMap(Scope.Attr.checksumVersion.toString(), version), { -> return originalChange.generateCheckSum(version) } as Scope.ScopedRunnerWithReturn)
        changeWithDbms.setDbms("postgresql")
        CheckSum changeWithDbmsCheckSum = changeWithDbms.generateCheckSum(version)

        then:
        changeWithoutDbmsCheckSum.toString() == originalChecksum
        changeWithDbmsCheckSum.toString() == updatedChecksum

        where:
        version | originalChecksum | updatedChecksum
        ChecksumVersion.V8 | "8:d41d8cd98f00b204e9800998ecf8427e" | "8:d41d8cd98f00b204e9800998ecf8427e"
        ChecksumVersion.latest() | "9:d41d8cd98f00b204e9800998ecf8427e" | "9:d41d8cd98f00b204e9800998ecf8427e"
    }

    @Unroll
    def "comment checksum generation - #version"(ChecksumVersion version, String originalChecksum, String updatedChecksum) {
        when:
        ChangeSet originalChange = new ChangeSet("testId", "testAuthor", false, false, "path/changelog", null, null, null)
        ChangeSet changeWithComment = originalChange

        CheckSum changeWithoutCommentCheckSum = Scope.child(Collections.singletonMap(Scope.Attr.checksumVersion.toString(), version), { -> return originalChange.generateCheckSum(version) } as Scope.ScopedRunnerWithReturn)
        changeWithComment.setComments("This is a test comment")
        CheckSum changeWithCommentCheckSum = changeWithComment.generateCheckSum(version)

        then:
        changeWithoutCommentCheckSum.toString() == originalChecksum
        changeWithCommentCheckSum.toString() == updatedChecksum

        where:
        version | originalChecksum | updatedChecksum
        ChecksumVersion.V8 | "8:d41d8cd98f00b204e9800998ecf8427e" | "8:d41d8cd98f00b204e9800998ecf8427e"
        ChecksumVersion.latest() | "9:d41d8cd98f00b204e9800998ecf8427e" | "9:d41d8cd98f00b204e9800998ecf8427e"
    }

    @DatabaseChange(name = "exampleParamlessAbstractChange", description = "Used for the AbstractChangeTest unit test", priority = 1)
    private static class ExampleParamlessAbstractChange extends AbstractChange {

        @Override
        String getConfirmationMessage() {
            return "Test Confirmation Message"
        }

        @Override
        SqlStatement[] generateStatements(Database database) {
            return null
        }

        @Override
        String getSerializedObjectNamespace() {
            return STANDARD_CHANGELOG_NAMESPACE
        }

    }
}
