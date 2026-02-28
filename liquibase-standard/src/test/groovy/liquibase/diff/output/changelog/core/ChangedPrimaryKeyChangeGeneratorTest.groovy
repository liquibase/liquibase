package liquibase.diff.output.changelog.core

import liquibase.change.core.AddPrimaryKeyChange
import liquibase.change.core.DropPrimaryKeyChange
import liquibase.database.core.H2Database
import liquibase.diff.ObjectDifferences
import liquibase.diff.compare.CompareControl
import liquibase.diff.output.DiffOutputControl
import liquibase.structure.core.Column
import liquibase.structure.core.PrimaryKey
import liquibase.structure.core.Table
import spock.lang.Specification

/**
 * Tests for ChangedPrimaryKeyChangeGenerator to verify it correctly uses comparison object
 * names for drop statements when primary key names differ between databases.
 *
 * This addresses issue #7461.
 */
class ChangedPrimaryKeyChangeGeneratorTest extends Specification {

    def "fixChanged uses comparison object name for drop statement when PK names differ"() {
        given: "Two PrimaryKey objects with different constraint names"
        def referenceConstraintName = "PK_REFERENCE_NAME"
        def comparisonConstraintName = "PK_COMPARISON_NAME"

        // Reference PK (from reference database - desired state)
        def referencePk = new PrimaryKey()
        referencePk.setName(referenceConstraintName)
        referencePk.setTable(new Table(null, "public", "test_table"))
        referencePk.addColumn(0, new Column("id"))

        // Comparison PK (from target database - current state)
        def comparisonPk = new PrimaryKey()
        comparisonPk.setName(comparisonConstraintName)
        comparisonPk.setTable(new Table(null, "public", "test_table"))
        comparisonPk.addColumn(0, new Column("id"))

        and: "ObjectDifferences with both objects"
        def compareControl = new CompareControl()
        def differences = new ObjectDifferences(compareControl, referencePk, comparisonPk)
        differences.addDifference("name", referenceConstraintName, comparisonConstraintName)

        and: "Generator and databases"
        def generator = new ChangedPrimaryKeyChangeGenerator()
        def outputControl = new DiffOutputControl()
        def referenceDatabase = new H2Database()
        def comparisonDatabase = new H2Database()

        when: "Generating changes"
        def changes = generator.fixChanged(referencePk, differences, outputControl, referenceDatabase, comparisonDatabase, null)

        then: "Should have 2 changes: drop and add"
        changes != null
        changes.length == 2

        and: "Drop statement uses COMPARISON database's constraint name (what exists in target DB)"
        changes[0] instanceof DropPrimaryKeyChange
        def dropChange = (DropPrimaryKeyChange) changes[0]
        dropChange.getConstraintName() == comparisonConstraintName

        and: "Add statement uses REFERENCE database's constraint name (desired state)"
        changes[1] instanceof AddPrimaryKeyChange
        def addChange = (AddPrimaryKeyChange) changes[1]
        addChange.getConstraintName() == referenceConstraintName
    }

    def "fixChanged fallbacks to reference object when comparison object is null for backward compatibility"() {
        given: "PrimaryKey with only reference object (old behavior)"
        def constraintName = "PK_TEST"

        def referencePk = new PrimaryKey()
        referencePk.setName(constraintName)
        referencePk.setTable(new Table(null, "public", "test_table"))
        referencePk.addColumn(0, new Column("id"))

        and: "ObjectDifferences with null comparison object (backward compatibility)"
        def compareControl = new CompareControl()
        def differences = new ObjectDifferences(compareControl)  // Old deprecated constructor
        differences.addDifference("someField", "oldValue", "newValue")

        and: "Generator and databases"
        def generator = new ChangedPrimaryKeyChangeGenerator()
        def outputControl = new DiffOutputControl()
        def referenceDatabase = new H2Database()
        def comparisonDatabase = new H2Database()

        when: "Generating changes"
        def changes = generator.fixChanged(referencePk, differences, outputControl, referenceDatabase, comparisonDatabase, null)

        then: "Should fallback to reference object for both drop and add"
        changes != null
        changes.length == 2

        and: "Drop uses reference object name when comparison is null"
        changes[0] instanceof DropPrimaryKeyChange
        def dropChange = (DropPrimaryKeyChange) changes[0]
        dropChange.getConstraintName() == constraintName

        and: "Add uses reference object name"
        changes[1] instanceof AddPrimaryKeyChange
        def addChange = (AddPrimaryKeyChange) changes[1]
        addChange.getConstraintName() == constraintName
    }

    def "fixChanged respects catalog and schema settings from comparison object"() {
        given: "Two PrimaryKey objects with different names in different schemas"
        def referenceConstraintName = "PK_REF"
        def comparisonConstraintName = "PK_COMP"

        def referencePk = new PrimaryKey()
        referencePk.setName(referenceConstraintName)
        referencePk.setTable(new Table("ref_catalog", "ref_schema", "test_table"))
        referencePk.addColumn(0, new Column("id"))

        def comparisonPk = new PrimaryKey()
        comparisonPk.setName(comparisonConstraintName)
        comparisonPk.setTable(new Table("comp_catalog", "comp_schema", "test_table"))
        comparisonPk.addColumn(0, new Column("id"))

        and: "ObjectDifferences with both objects"
        def compareControl = new CompareControl()
        def differences = new ObjectDifferences(compareControl, referencePk, comparisonPk)
        differences.addDifference("name", referenceConstraintName, comparisonConstraintName)

        and: "Generator with catalog and schema inclusion"
        def generator = new ChangedPrimaryKeyChangeGenerator()
        def outputControl = new DiffOutputControl(true, true, false, null)
        def referenceDatabase = new H2Database()
        def comparisonDatabase = new H2Database()

        when: "Generating changes"
        def changes = generator.fixChanged(referencePk, differences, outputControl, referenceDatabase, comparisonDatabase, null)

        then: "Drop uses comparison catalog and schema"
        changes != null
        changes.length == 2
        def dropChange = (DropPrimaryKeyChange) changes[0]
        dropChange.getCatalogName() == "comp_catalog"
        dropChange.getSchemaName() == "comp_schema"

        and: "Add uses reference catalog and schema"
        def addChange = (AddPrimaryKeyChange) changes[1]
        addChange.getCatalogName() == "ref_catalog"
        addChange.getSchemaName() == "ref_schema"
    }
}
