package liquibase.extension.testing

import liquibase.extension.testing.command.CommandTests
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite.class)
@Suite.SuiteClasses([CommandTests])
abstract class BaseLiquibaseExtensionTests {
}
