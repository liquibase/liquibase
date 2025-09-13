import liquibase.Scope
import liquibase.command.CommandScope
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.UpdateCountCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.resource.SearchPathResourceAccessor
import org.apache.commons.io.FileUtils
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files
import java.nio.file.Path
class H2Spec extends Specification {

	@Unroll("#featureName: #params")
	def testCaseingOption() {
		when:
		def scopeSettings = [
			(Scope.Attr.resourceAccessor.name()): new SearchPathResourceAccessor(".,target/test-classes")
		]
		String fileName = './h2/db'
		Files.deleteIfExists(Path.of(fileName + '.mv.db'))

		Scope.child(scopeSettings, {
			CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
			commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, "jdbc:h2:$fileName;$params")
			commandScope.addArgumentValue(UpdateCountCommandStep.CHANGELOG_FILE_ARG, 'changelogs/h2/complete/included.changelog.xml')
			commandScope.execute()
			commandScope.execute()
		} as Scope.ScopedRunnerWithReturn<Void>)

		then:
		noExceptionThrown()

		cleanup:
		FileUtils.deleteQuietly(new File('./h2'))

		where:
		params << ['DATABASE_TO_UPPER=false', 'DATABASE_TO_LOWER=true']

	}
}