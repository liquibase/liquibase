package liquibase.diff.output.changelog.core

import liquibase.change.core.AddForeignKeyConstraintChange
import liquibase.change.core.DropForeignKeyConstraintChange
import liquibase.database.core.H2Database
import liquibase.diff.ObjectDifferences
import liquibase.diff.compare.CompareControl
import liquibase.diff.output.DiffOutputControl
import liquibase.structure.core.Column
import liquibase.structure.core.ForeignKey
import liquibase.structure.core.Table
import spock.lang.Specification

/**
 * Tests for ChangedForeignKeyChangeGenerator to verify it correctly uses comparison object
 * names for drop statements when foreign key constraint names differ between databases.
 *
 * This addresses issue #7461.
 */
class ChangedForeignKeyChangeGeneratorTest extends Specification {

    def "fixChanged uses comparison object name for drop statement when FK names differ"() {
        given: "Two ForeignKey objects with different constraint names"
        def referenceConstraintName = "FK_REFERENCE_NAME"
        def comparisonConstraintName = "FK_COMPARISON_NAME"

        // Reference FK (from reference database - desired state)
        def referenceFk = new ForeignKey()
        referenceFk.setName(referenceConstraintName)
        referenceFk.setForeignKeyTable(new Table(null, "public", "parent_table"))
        referenceFk.addForeignKeyColumn(new Column("parent_id"))
        referenceFk.setPrimaryKeyTable(new Table(null, "public", "child_table"))
        referenceFk.addPrimaryKeyColumn(new Column("id"))

        // Comparison FK (from target database - current state)
        def comparisonFk = new ForeignKey()
        comparisonFk.setName(comparisonConstraintName)
        comparisonFk.setForeignKeyTable(new Table(null, "public", "parent_table"))
        comparisonFk.addForeignKeyColumn(new Column("parent_id"))
        comparisonFk.setPrimaryKeyTable(new Table(null, "public", "child_table"))
        comparisonFk.addPrimaryKeyColumn(new Column("id"))

        and: "ObjectDifferences with both objects"
        def compareControl = new CompareControl()
        def differences = new ObjectDifferences(compareControl, referenceFk, comparisonFk)
        differences.addDifference("name", referenceConstraintName, comparisonConstraintName)

        and: "Generator and databases"
        def generator = new ChangedForeignKeyChangeGenerator()
        def outputControl = new DiffOutputControl()
        def referenceDatabase = new H2Database()
        def comparisonDatabase = new H2Database()

        when: "Generating changes"
        def changes = generator.fixChanged(referenceFk, differences, outputControl, referenceDatabase, comparisonDatabase, null)

        then: "Should have 2 changes: drop and add"
        changes != null
        changes.length == 2

        and: "Drop statement uses COMPARISON database's constraint name (what exists in target DB)"
        changes[0] instanceof DropForeignKeyConstraintChange
        def dropChange = (DropForeignKeyConstraintChange) changes[0]
        dropChange.getConstraintName() == comparisonConstraintName

        and: "Add statement uses REFERENCE database's constraint name (desired state)"
        changes[1] instanceof AddForeignKeyConstraintChange
        def addChange = (AddForeignKeyConstraintChange) changes[1]
        addChange.getConstraintName() == referenceConstraintName
    }

    def "fixChanged fallbacks to reference object when comparison object is null for backward compatibility"() {
        given: "FK with only reference object (old behavior)"
        def constraintName = "FK_TEST"

        def referenceFk = new ForeignKey()
        referenceFk.setName(constraintName)
        referenceFk.setForeignKeyTable(new Table(null, "public", "parent_table"))
        referenceFk.addForeignKeyColumn(new Column("parent_id"))
        referenceFk.setPrimaryKeyTable(new Table(null, "public", "child_table"))
        referenceFk.addPrimaryKeyColumn(new Column("id"))

        and: "ObjectDifferences with null comparison object (backward compatibility)"
        def compareControl = new CompareControl()
        def differences = new ObjectDifferences(compareControl)  // Old deprecated constructor

        and: "Generator and databases"
        def generator = new ChangedForeignKeyChangeGenerator()
        def outputControl = new DiffOutputControl()
        def referenceDatabase = new H2Database()
        def comparisonDatabase = new H2Database()

        when: "Generating changes"
        def changes = generator.fixChanged(referenceFk, differences, outputControl, referenceDatabase, comparisonDatabase, null)

        then: "Should fallback to reference object for both drop and add"
        changes != null
        changes.length == 2

        and: "Drop uses reference object name when comparison is null"
        changes[0] instanceof DropForeignKeyConstraintChange
        def dropChange = (DropForeignKeyConstraintChange) changes[0]
        dropChange.getConstraintName() == constraintName

        and: "Add uses reference object name"
        changes[1] instanceof AddForeignKeyConstraintChange
        def addChange = (AddForeignKeyConstraintChange) changes[1]
        addChange.getConstraintName() == constraintName
    }

    def "fixChanged respects catalog and schema settings from comparison object"() {
        given: "Two ForeignKey objects with different constraint names in different schemas"
        def referenceConstraintName = "FK_REF"
        def comparisonConstraintName = "FK_COMP"

        def referenceFk = new ForeignKey()
        referenceFk.setName(referenceConstraintName)
        referenceFk.setForeignKeyTable(new Table("ref_catalog", "ref_schema", "parent_table"))
        referenceFk.addForeignKeyColumn(new Column("parent_id"))
        referenceFk.setPrimaryKeyTable(new Table("ref_catalog", "ref_schema", "child_table"))
        referenceFk.addPrimaryKeyColumn(new Column("id"))

        def comparisonFk = new ForeignKey()
        comparisonFk.setName(comparisonConstraintName)
        comparisonFk.setForeignKeyTable(new Table("comp_catalog", "comp_schema", "parent_table"))
        comparisonFk.addForeignKeyColumn(new Column("parent_id"))
        comparisonFk.setPrimaryKeyTable(new Table("comp_catalog", "comp_schema", "child_table"))
        comparisonFk.addPrimaryKeyColumn(new Column("id"))

        and: "ObjectDifferences with both objects"
        def compareControl = new CompareControl()
        def differences = new ObjectDifferences(compareControl, referenceFk, comparisonFk)
        differences.addDifference("name", referenceConstraintName, comparisonConstraintName)

        and: "Generator with catalog and schema inclusion"
        def generator = new ChangedForeignKeyChangeGenerator()
        def outputControl = new DiffOutputControl(true, true, false, null)
        def referenceDatabase = new H2Database()
        def comparisonDatabase = new H2Database()

        when: "Generating changes"
        def changes = generator.fixChanged(referenceFk, differences, outputControl, referenceDatabase, comparisonDatabase, null)

        then: "Drop uses comparison catalog and schema"
        changes != null
        changes.length == 2
        def dropChange = (DropForeignKeyConstraintChange) changes[0]
        dropChange.getBaseTableCatalogName() == "comp_catalog"
        dropChange.getBaseTableSchemaName() == "comp_schema"

        and: "Add uses reference catalog and schema"
        def addChange = (AddForeignKeyConstraintChange) changes[1]
        addChange.getBaseTableCatalogName() == "ref_catalog"
        addChange.getBaseTableSchemaName() == "ref_schema"
    }
}
