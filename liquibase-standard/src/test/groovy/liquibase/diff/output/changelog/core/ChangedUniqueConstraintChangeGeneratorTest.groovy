package liquibase.diff.output.changelog.core

import liquibase.change.core.AddUniqueConstraintChange
import liquibase.change.core.DropUniqueConstraintChange
import liquibase.database.core.H2Database
import liquibase.diff.ObjectDifferences
import liquibase.diff.compare.CompareControl
import liquibase.diff.output.DiffOutputControl
import liquibase.structure.core.Column
import liquibase.structure.core.Table
import liquibase.structure.core.UniqueConstraint
import spock.lang.Specification

/**
 * Tests for ChangedUniqueConstraintChangeGenerator to verify it correctly uses comparison object
 * names for drop statements when unique constraint names differ between databases.
 *
 * This addresses issue #7461.
 */
class ChangedUniqueConstraintChangeGeneratorTest extends Specification {

    def "fixChanged uses comparison object name for drop statement when UC names differ"() {
        given: "Two UniqueConstraint objects with different constraint names"
        def referenceConstraintName = "UC_REFERENCE_NAME"
        def comparisonConstraintName = "UC_COMPARISON_NAME"

        // Reference UC (from reference database - desired state)
        def referenceUc = new UniqueConstraint()
        referenceUc.setName(referenceConstraintName)
        referenceUc.setRelation(new Table(null, "public", "test_table"))
        referenceUc.addColumn(0, new Column("unique_col"))

        // Comparison UC (from target database - current state)
        def comparisonUc = new UniqueConstraint()
        comparisonUc.setName(comparisonConstraintName)
        comparisonUc.setRelation(new Table(null, "public", "test_table"))
        comparisonUc.addColumn(0, new Column("unique_col"))

        and: "ObjectDifferences with both objects"
        def compareControl = new CompareControl()
        def differences = new ObjectDifferences(compareControl, referenceUc, comparisonUc)
        differences.addDifference("name", referenceConstraintName, comparisonConstraintName)

        and: "Generator and databases"
        def generator = new ChangedUniqueConstraintChangeGenerator()
        def outputControl = new DiffOutputControl()
        def referenceDatabase = new H2Database()
        def comparisonDatabase = new H2Database()

        when: "Generating changes"
        def changes = generator.fixChanged(referenceUc, differences, outputControl, referenceDatabase, comparisonDatabase, null)

        then: "Should have 2 changes: drop and add"
        changes != null
        changes.length == 2

        and: "Drop statement uses COMPARISON database's constraint name (what exists in target DB)"
        changes[0] instanceof DropUniqueConstraintChange
        def dropChange = (DropUniqueConstraintChange) changes[0]
        dropChange.getConstraintName() == comparisonConstraintName

        and: "Add statement uses REFERENCE database's constraint name (desired state)"
        changes[1] instanceof AddUniqueConstraintChange
        def addChange = (AddUniqueConstraintChange) changes[1]
        addChange.getConstraintName() == referenceConstraintName
    }

    def "fixChanged fallbacks to reference object when comparison object is null for backward compatibility"() {
        given: "UniqueConstraint with only reference object (old behavior)"
        def constraintName = "UC_TEST"

        def referenceUc = new UniqueConstraint()
        referenceUc.setName(constraintName)
        referenceUc.setRelation(new Table(null, "public", "test_table"))
        referenceUc.addColumn(0, new Column("unique_col"))

        and: "ObjectDifferences with null comparison object (backward compatibility)"
        def compareControl = new CompareControl()
        def differences = new ObjectDifferences(compareControl)  // Old deprecated constructor
        differences.addDifference("someField", "oldValue", "newValue")

        and: "Generator and databases"
        def generator = new ChangedUniqueConstraintChangeGenerator()
        def outputControl = new DiffOutputControl()
        def referenceDatabase = new H2Database()
        def comparisonDatabase = new H2Database()

        when: "Generating changes"
        def changes = generator.fixChanged(referenceUc, differences, outputControl, referenceDatabase, comparisonDatabase, null)

        then: "Should fallback to reference object for both drop and add"
        changes != null
        changes.length == 2

        and: "Drop uses reference object name when comparison is null"
        changes[0] instanceof DropUniqueConstraintChange
        def dropChange = (DropUniqueConstraintChange) changes[0]
        dropChange.getConstraintName() == constraintName

        and: "Add uses reference object name"
        changes[1] instanceof AddUniqueConstraintChange
        def addChange = (AddUniqueConstraintChange) changes[1]
        addChange.getConstraintName() == constraintName
    }

    def "fixChanged respects catalog and schema settings from comparison object"() {
        given: "Two UniqueConstraint objects with different names in different schemas"
        def referenceConstraintName = "UC_REF"
        def comparisonConstraintName = "UC_COMP"

        def referenceUc = new UniqueConstraint()
        referenceUc.setName(referenceConstraintName)
        referenceUc.setRelation(new Table("ref_catalog", "ref_schema", "test_table"))
        referenceUc.addColumn(0, new Column("unique_col"))

        def comparisonUc = new UniqueConstraint()
        comparisonUc.setName(comparisonConstraintName)
        comparisonUc.setRelation(new Table("comp_catalog", "comp_schema", "test_table"))
        comparisonUc.addColumn(0, new Column("unique_col"))

        and: "ObjectDifferences with both objects"
        def compareControl = new CompareControl()
        def differences = new ObjectDifferences(compareControl, referenceUc, comparisonUc)
        differences.addDifference("name", referenceConstraintName, comparisonConstraintName)

        and: "Generator with catalog and schema inclusion"
        def generator = new ChangedUniqueConstraintChangeGenerator()
        def outputControl = new DiffOutputControl(true, true, false, null)
        def referenceDatabase = new H2Database()
        def comparisonDatabase = new H2Database()

        when: "Generating changes"
        def changes = generator.fixChanged(referenceUc, differences, outputControl, referenceDatabase, comparisonDatabase, null)

        then: "Drop uses comparison catalog and schema"
        changes != null
        changes.length == 2
        def dropChange = (DropUniqueConstraintChange) changes[0]
        dropChange.getCatalogName() == "comp_catalog"
        dropChange.getSchemaName() == "comp_schema"

        and: "Add uses reference catalog and schema"
        def addChange = (AddUniqueConstraintChange) changes[1]
        addChange.getCatalogName() == "ref_catalog"
        addChange.getSchemaName() == "ref_schema"
    }
}
