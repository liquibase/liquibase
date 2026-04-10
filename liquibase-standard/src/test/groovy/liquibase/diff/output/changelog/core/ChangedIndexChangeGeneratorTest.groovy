package liquibase.diff.output.changelog.core

import liquibase.change.core.CreateIndexChange
import liquibase.change.core.DropIndexChange
import liquibase.database.core.H2Database
import liquibase.diff.ObjectDifferences
import liquibase.diff.compare.CompareControl
import liquibase.diff.output.DiffOutputControl
import liquibase.structure.core.Column
import liquibase.structure.core.Index
import liquibase.structure.core.Table
import spock.lang.Specification

/**
 * Tests for ChangedIndexChangeGenerator to verify it correctly uses comparison object
 * names for drop statements when index names differ between databases.
 *
 * This addresses issue #7461.
 */
class ChangedIndexChangeGeneratorTest extends Specification {

    def "fixChanged uses comparison object name for drop statement when index names differ"() {
        given: "Two Index objects with different names"
        def referenceIndexName = "IDX_REFERENCE_NAME"
        def comparisonIndexName = "IDX_COMPARISON_NAME"

        // Reference Index (from reference database - desired state)
        def referenceIndex = new Index()
        referenceIndex.setName(referenceIndexName)
        referenceIndex.setRelation(new Table(null, "public", "test_table"))
        referenceIndex.addColumn(new Column("column1"))
        referenceIndex.setUnique(false)

        // Comparison Index (from target database - current state)
        def comparisonIndex = new Index()
        comparisonIndex.setName(comparisonIndexName)
        comparisonIndex.setRelation(new Table(null, "public", "test_table"))
        comparisonIndex.addColumn(new Column("column1"))
        comparisonIndex.setUnique(false)

        and: "ObjectDifferences with both objects"
        def compareControl = new CompareControl()
        def differences = new ObjectDifferences(compareControl, referenceIndex, comparisonIndex)
        differences.addDifference("name", referenceIndexName, comparisonIndexName)

        and: "Generator and databases"
        def generator = new ChangedIndexChangeGenerator()
        def outputControl = new DiffOutputControl()
        def referenceDatabase = new H2Database()
        def comparisonDatabase = new H2Database()

        when: "Generating changes"
        def changes = generator.fixChanged(referenceIndex, differences, outputControl, referenceDatabase, comparisonDatabase, null)

        then: "Should have 2 changes: drop and create"
        changes != null
        changes.length == 2

        and: "Drop statement uses COMPARISON database's index name (what exists in target DB)"
        changes[0] instanceof DropIndexChange
        def dropChange = (DropIndexChange) changes[0]
        dropChange.getIndexName() == comparisonIndexName

        and: "Create statement uses REFERENCE database's index name (desired state)"
        changes[1] instanceof CreateIndexChange
        def createChange = (CreateIndexChange) changes[1]
        createChange.getIndexName() == referenceIndexName
    }

    def "fixChanged fallbacks to reference object when comparison object is null for backward compatibility"() {
        given: "Index with only reference object (old behavior)"
        def indexName = "IDX_TEST"

        def referenceIndex = new Index()
        referenceIndex.setName(indexName)
        referenceIndex.setRelation(new Table(null, "public", "test_table"))
        referenceIndex.addColumn(new Column("column1"))
        referenceIndex.setUnique(false)

        and: "ObjectDifferences with null comparison object (backward compatibility)"
        def compareControl = new CompareControl()
        def differences = new ObjectDifferences(compareControl)  // Old deprecated constructor
        differences.addDifference("someField", "oldValue", "newValue")

        and: "Generator and databases"
        def generator = new ChangedIndexChangeGenerator()
        def outputControl = new DiffOutputControl()
        def referenceDatabase = new H2Database()
        def comparisonDatabase = new H2Database()

        when: "Generating changes"
        def changes = generator.fixChanged(referenceIndex, differences, outputControl, referenceDatabase, comparisonDatabase, null)

        then: "Should fallback to reference object for both drop and create"
        changes != null
        changes.length == 2

        and: "Drop uses reference object name when comparison is null"
        changes[0] instanceof DropIndexChange
        def dropChange = (DropIndexChange) changes[0]
        dropChange.getIndexName() == indexName

        and: "Create uses reference object name"
        changes[1] instanceof CreateIndexChange
        def createChange = (CreateIndexChange) changes[1]
        createChange.getIndexName() == indexName
    }

    def "fixChanged respects catalog and schema settings from comparison object"() {
        given: "Two Index objects with different names in different schemas"
        def referenceIndexName = "IDX_REF"
        def comparisonIndexName = "IDX_COMP"

        def referenceIndex = new Index()
        referenceIndex.setName(referenceIndexName)
        referenceIndex.setRelation(new Table("ref_catalog", "ref_schema", "test_table"))
        referenceIndex.addColumn(new Column("column1"))
        referenceIndex.setUnique(false)

        def comparisonIndex = new Index()
        comparisonIndex.setName(comparisonIndexName)
        comparisonIndex.setRelation(new Table("comp_catalog", "comp_schema", "test_table"))
        comparisonIndex.addColumn(new Column("column1"))
        comparisonIndex.setUnique(false)

        and: "ObjectDifferences with both objects"
        def compareControl = new CompareControl()
        def differences = new ObjectDifferences(compareControl, referenceIndex, comparisonIndex)
        differences.addDifference("name", referenceIndexName, comparisonIndexName)

        and: "Generator with catalog and schema inclusion"
        def generator = new ChangedIndexChangeGenerator()
        def outputControl = new DiffOutputControl(true, true, false, null)
        def referenceDatabase = new H2Database()
        def comparisonDatabase = new H2Database()

        when: "Generating changes"
        def changes = generator.fixChanged(referenceIndex, differences, outputControl, referenceDatabase, comparisonDatabase, null)

        then: "Drop uses comparison catalog and schema"
        changes != null
        changes.length == 2
        def dropChange = (DropIndexChange) changes[0]
        dropChange.getCatalogName() == "comp_catalog"
        dropChange.getSchemaName() == "comp_schema"

        and: "Create uses reference catalog and schema"
        def createChange = (CreateIndexChange) changes[1]
        createChange.getCatalogName() == "ref_catalog"
        createChange.getSchemaName() == "ref_schema"
    }
}
