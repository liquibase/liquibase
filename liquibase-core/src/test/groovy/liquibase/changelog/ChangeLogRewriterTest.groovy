package liquibase.changelog

import liquibase.parser.ChangeLogParser
import liquibase.parser.ChangeLogParserFactory
import liquibase.resource.DirectoryResourceAccessor
import liquibase.resource.ResourceAccessor
import liquibase.util.FileUtil
import spock.lang.Specification

import java.nio.file.Paths
import java.util.regex.Matcher
import java.util.regex.Pattern

class ChangeLogRewriterTest extends Specification {
    def "register changelog"() {
        when:
        //
        // Create a new file that we will modify to add the changeLogId
        //
        String changeLogId = UUID.randomUUID().toString()
        String fileName = file + extension
        URL url = Thread.currentThread().getContextClassLoader().getResource(fileName)
        File changeLogFile = new File(url.toURI())
        File outputFile = File.createTempFile("registerChangelog-", extension, new File("target/test-classes"))
        outputFile.deleteOnExit()
        DatabaseChangeLog changeLog = new DatabaseChangeLog(outputFile.getAbsolutePath())
        String contents = FileUtil.getContents(changeLogFile)
        FileUtil.write(contents, outputFile)

        //
        // Add the changeLogId
        //
        ChangelogRewriter.ChangeLogRewriterResult result = ChangelogRewriter.addChangeLogId(outputFile.getName(), changeLogId, changeLog)

        //
        // Create a matcher for the changeLogId pattern and the exact ID
        //
        contents = FileUtil.getContents(outputFile.getAbsoluteFile())
        Pattern pattern = Pattern.compile(patternString,Pattern.DOTALL)
        Matcher matcher = pattern.matcher(contents)
        Matcher idMatcher = Pattern.compile(".*" + changeLogId + ".*", Pattern.DOTALL).matcher(contents)

        //
        // Make sure we can parse the file
        //
        ResourceAccessor resourceAccessor = new DirectoryResourceAccessor(Paths.get("target/test-classes").toAbsolutePath().toFile())
        ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser(outputFile.getName(), resourceAccessor)
        DatabaseChangeLog newChangeLog = parser.parse(outputFile.getName(), new ChangeLogParameters(), resourceAccessor)

        then:
        result.success
        matcher.matches()
        idMatcher.matches()
        changeLog.getChangeLogId() != null
        newChangeLog.getChangeLogId() != null

        where:
        file                        | extension   | patternString
        "liquibase/test-changelog"  | ".xml"      | ".*changeLogId=.*\\W\\/>.*"
        "liquibase/test-changelog"  | ".json"     | ".*\"changeLogId\":.*"
        "liquibase/test-changelog"  | ".yml"      | ".*- changeLogId: .*\$"
        "liquibase/test-changelog"  | ".sql"      | ".*liquibase formatted sql changeLogId:.*\$"
    }
    def "register changelog readonly"() {
        when:
        //
        // Create a new file that we will modify to add the changeLogId
        //
        String changeLogId = UUID.randomUUID().toString()
        String fileName = file + extension
        URL url = Thread.currentThread().getContextClassLoader().getResource(fileName)
        File changeLogFile = new File(url.toURI())
        File outputFile = File.createTempFile("registerChangelog-", extension, new File("target/test-classes"))
        outputFile.deleteOnExit()
        DatabaseChangeLog changeLog = new DatabaseChangeLog(outputFile.getAbsolutePath())
        String contents = FileUtil.getContents(changeLogFile)
        FileUtil.write(contents, outputFile)
        outputFile.setReadOnly()

        //
        // Add the changeLogId
        //
        ChangelogRewriter.ChangeLogRewriterResult result = ChangelogRewriter.addChangeLogId(outputFile.getName(), changeLogId, changeLog)

        //
        // Create a matcher for the changeLogId pattern and the exact ID
        //
        contents = FileUtil.getContents(outputFile.getAbsoluteFile())
        Pattern pattern = Pattern.compile(patternString,Pattern.DOTALL)
        Matcher matcher = pattern.matcher(contents)
        Matcher idMatcher = Pattern.compile(".*" + changeLogId + ".*", Pattern.DOTALL).matcher(contents)

        //
        // Make sure we can parse the file
        //
        ResourceAccessor resourceAccessor = new DirectoryResourceAccessor(Paths.get("target/test-classes").toAbsolutePath().toFile())
        ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser(outputFile.getName(), resourceAccessor)
        changeLog = parser.parse(outputFile.getName(), new ChangeLogParameters(), resourceAccessor)

        then:
        !result.success
        result.message.matches(".*was not registered due to an error.*")
        !matcher.matches()
        !idMatcher.matches()
        changeLog.getChangeLogId() == null

        where:
        file                        | extension   | patternString
        "liquibase/test-changelog"  | ".xml"      | ".*changeLogId=.*\\W\\/>.*"
    }

    def "deactivate changelog"() {
        when:
        //
        // Create a new file that we will modify to remove the changeLogId
        //
        String fileName = file + extension
        URL url = Thread.currentThread().getContextClassLoader().getResource(fileName)
        File changeLogFile = new File(url.toURI())
        File outputFile = File.createTempFile("registeredChangelog-", extension, new File("target/test-classes"))
        outputFile.deleteOnExit()
        String contents = FileUtil.getContents(changeLogFile)
        FileUtil.write(contents, outputFile)

        //
        // Parse the file to grab the changeLogId
        //
        ResourceAccessor resourceAccessor = new DirectoryResourceAccessor(Paths.get("target/test-classes").toAbsolutePath().toFile())
        ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser(outputFile.getName(), resourceAccessor)
        DatabaseChangeLog changeLog = parser.parse(outputFile.getName(), new ChangeLogParameters(), resourceAccessor)
        String changeLogId = changeLog.getChangeLogId()

        //
        // Remove the changeLogId
        //
        ChangelogRewriter.ChangeLogRewriterResult result = ChangelogRewriter.removeChangeLogId(outputFile.getName(), changeLogId, changeLog)

        //
        // Make sure we can parse it
        //
        changeLog = parser.parse(outputFile.getName(), new ChangeLogParameters(), resourceAccessor)

        //
        // Create a matcher for the ID string
        //
        contents = FileUtil.getContents(outputFile.getAbsoluteFile())
        Matcher idMatcher = Pattern.compile(changeLogId, Pattern.DOTALL).matcher(contents)

        then:
        result.success
        changeLog.getChangeLogId() == null
        ! idMatcher.matches()

        where:
        file                              | extension
        "liquibase/registered-changelog"  | ".xml"
        "liquibase/registered-changelog"  | ".json"
        "liquibase/registered-changelog"  | ".yml"
        "liquibase/registered-changelog"  | ".sql"
    }
}
