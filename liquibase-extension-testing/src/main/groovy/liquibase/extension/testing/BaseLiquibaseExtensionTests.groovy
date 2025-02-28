package liquibase.extension.testing

import liquibase.extension.testing.command.CommandTests
import org.junit.platform.suite.api.SelectClasses
import org.junit.platform.suite.api.Suite

@Suite
@SelectClasses([CommandTests])
abstract class BaseLiquibaseExtensionTests {
}
