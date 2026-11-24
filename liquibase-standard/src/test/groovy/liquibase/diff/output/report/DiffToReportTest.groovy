package liquibase.diff.output.report

import liquibase.database.core.MockDatabase
import liquibase.diff.compare.CompareControl
import liquibase.diff.core.StandardDiffGenerator
import liquibase.snapshot.EmptyDatabaseSnapshot
import liquibase.snapshot.SnapshotControl
import liquibase.structure.core.Table
import spock.lang.Specification

class DiffToReportTest extends Specification {

    def "the report lists only the types its diff compared, not every type of the compare control"() {
        given:
        def database = new MockDatabase()
        def tableOnly = new EmptyDatabaseSnapshot(database, new SnapshotControl(database, Table))
        def compareControl = new CompareControl()
        def diffResult = new StandardDiffGenerator().compare(tableOnly, tableOnly, compareControl)
        def output = new ByteArrayOutputStream()

        when:
        new DiffToReport(diffResult, new PrintStream(output, true, "UTF-8")).print()
        def missingSections = output.toString("UTF-8").readLines().findAll { it.startsWith("Missing ") }

        then:
        compareControl.getComparedTypes().size() > 1
        missingSections == ["Missing Table(s): NONE"]
    }
}
