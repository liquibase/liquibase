package liquibase.command

import liquibase.Scope
import liquibase.command.core.CalculateChecksumCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.database.core.MockDatabase
import liquibase.exception.CommandExecutionException
import liquibase.resource.SearchPathResourceAccessor
import liquibase.util.StringUtil
import spock.lang.Specification
import spock.lang.Unroll

class CalculateChecksumCommandStepTest extends Specification {

    def validateCheckSumIsCalculatedSuccessfully() {
        when:
        def generatedCheckSum = calculateCheckSum("tagged-changelog.xml", "1", "liquibase")

        then:
        StringUtil.isNotEmpty(generatedCheckSum.toString())
    }

    //Negative Tests
    @Unroll
    def "validate CheckSum is not calculated having a missing path argument"(String path) {
        when:
        def generatedCheckSum = calculateCheckSum(path, "1", "liquibase")

        then:
        StringUtil.isEmpty(generatedCheckSum)
        def e = thrown(CommandExecutionException)
        e.message.contains("Missing argument:  '--changeset-path'")

        where:
        path << ["", null]
    }

    @Unroll
    def "validate CheckSum is not calculated having a missing author argument"(String author) {
        when:
        def generatedCheckSum = calculateCheckSum("tagged-changelog.xml", "1", author)

        then:
        StringUtil.isEmpty(generatedCheckSum)
        def e = thrown(CommandExecutionException)
        e.message.contains("Missing argument:  '--changeset-author'")

        where:
        author << ["", null]
    }

    @Unroll
    def "validate CheckSum is not calculated having a missing Id argument"(String id) {
        when:
        def generatedCheckSum = calculateCheckSum("tagged-changelog.xml", id, "liquibase")

        then:
        StringUtil.isEmpty(generatedCheckSum)
        def e = thrown(CommandExecutionException)
        e.message.contains("Missing argument:  '--changeset-id'")

        where:
        id << ["", null]
    }

    def "validate error is returned when changesetIdentifier and the three separate arguments are provided at the same time"() {
        when:
        def resourceAccessor = new SearchPathResourceAccessor("target/test-classes/liquibase")
        def scopeSettings = [
                (Scope.Attr.resourceAccessor.name()): resourceAccessor
        ]

        def generatedCheckSum = ""

        Scope.child(scopeSettings, {
            CommandResults commandResults = new CommandScope("calculateChecksum")
                    .addArgumentValue(CalculateChecksumCommandStep.CHANGELOG_FILE_ARG, "tagged-changelog.xml")
                    .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, new MockDatabase())
                    .addArgumentValue(CalculateChecksumCommandStep.CHANGESET_PATH_ARG, "tagged-changelog.xml")
                    .addArgumentValue(CalculateChecksumCommandStep.CHANGESET_ID_ARG, "1")
                    .addArgumentValue(CalculateChecksumCommandStep.CHANGESET_AUTHOR_ARG, "liquibase")
                    .addArgumentValue(CalculateChecksumCommandStep.CHANGESET_IDENTIFIER_ARG, "tagged-changelog.xml::1::liquibase")
                    .execute()
            generatedCheckSum = commandResults.getResult("checksumResult")
        } as Scope.ScopedRunner)

        then:
        StringUtil.isEmpty(generatedCheckSum)
        def e = thrown(CommandExecutionException)
        e.message.contains("'--changeset-identifier' cannot be provided alongside other changeset arguments: '--changeset-id', '--changeset-path', '--changeset-author'")
    }

    private String calculateCheckSum(String path, String id, String author) {
        try {
            def resourceAccessor = new SearchPathResourceAccessor("target/test-classes/liquibase")
            def scopeSettings = [
                    (Scope.Attr.resourceAccessor.name()): resourceAccessor
            ]

            def generatedCheckSum = ""

            Scope.child(scopeSettings, {
                CommandResults commandResults = new CommandScope("calculateChecksum")
                        .addArgumentValue(CalculateChecksumCommandStep.CHANGELOG_FILE_ARG, "tagged-changelog.xml")
                        .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, new MockDatabase())
                        .addArgumentValue(CalculateChecksumCommandStep.CHANGESET_PATH_ARG, path)
                        .addArgumentValue(CalculateChecksumCommandStep.CHANGESET_ID_ARG, id)
                        .addArgumentValue(CalculateChecksumCommandStep.CHANGESET_AUTHOR_ARG, author)
                        .execute()
                generatedCheckSum = commandResults.getResult("checksumResult")
            } as Scope.ScopedRunner)

            return generatedCheckSum
        }
        catch (CommandExecutionException e) {
            throw e
        }

    }
}
