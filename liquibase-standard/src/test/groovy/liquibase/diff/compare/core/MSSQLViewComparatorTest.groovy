package liquibase.diff.compare.core

import liquibase.database.core.MSSQLDatabase
import liquibase.database.core.PostgresDatabase
import liquibase.diff.compare.CompareControl
import liquibase.diff.compare.DatabaseObjectComparator
import liquibase.diff.compare.DatabaseObjectComparatorChain
import liquibase.diff.compare.DatabaseObjectComparatorFactory
import liquibase.structure.core.Schema
import liquibase.structure.core.Table
import liquibase.structure.core.View
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit tests for MSSQLViewComparator.
 * Verifies that MSSQL view definitions differing only in schema qualification
 * and bracket quoting are not reported as different.
 */
class MSSQLViewComparatorTest extends Specification {

    private MSSQLViewComparator comparator = new MSSQLViewComparator()

    def "getPriority returns PRIORITY_DATABASE for View on MSSQLDatabase"() {
        expect:
        comparator.getPriority(View, new MSSQLDatabase()) == DatabaseObjectComparator.PRIORITY_DATABASE
    }

    def "getPriority returns PRIORITY_NONE for View on non-MSSQL database"() {
        expect:
        comparator.getPriority(View, new PostgresDatabase()) == DatabaseObjectComparator.PRIORITY_NONE
    }

    def "getPriority returns PRIORITY_NONE for non-View types on MSSQLDatabase"() {
        expect:
        comparator.getPriority(Table, new MSSQLDatabase()) == DatabaseObjectComparator.PRIORITY_NONE
    }

    @Unroll
    def "normalizeViewDefinition: '#description'"() {
        expect:
        MSSQLViewComparator.normalizeViewDefinition(input, schemaName) == expected

        where:
        description                              | input                                                              | schemaName | expected
        "strips [schema].[view] to view"         | "CREATE VIEW [dbo].[view_demo] AS SELECT 1"                        | "dbo"      | "CREATE VIEW view_demo AS SELECT 1"
        "strips schema.view to view"             | "CREATE VIEW dbo.view_demo AS SELECT 1"                            | "dbo"      | "CREATE VIEW view_demo AS SELECT 1"
        "strips [schema].[view] for non-dbo"     | "CREATE VIEW [sales].[VW_ORDER_DETAILS] AS SELECT 1"               | "sales"    | "CREATE VIEW VW_ORDER_DETAILS AS SELECT 1"
        "strips brackets-only view name"         | "CREATE VIEW [view_demo] AS SELECT 1"                              | "dbo"      | "CREATE VIEW view_demo AS SELECT 1"
        "leaves plain view name untouched"        | "CREATE VIEW view_demo AS SELECT 1"                                | "dbo"      | "CREATE VIEW view_demo AS SELECT 1"
        "handles null schema name"               | "CREATE VIEW [dbo].[view_demo] AS SELECT 1"                        | null       | "CREATE VIEW dbo.[view_demo] AS SELECT 1"
        "handles null definition"                | null                                                               | "dbo"      | null
        "preserves WITH SCHEMABINDING"           | "CREATE VIEW [dbo].[view_demo] WITH SCHEMABINDING AS SELECT 1"     | "dbo"      | "CREATE VIEW view_demo WITH SCHEMABINDING AS SELECT 1"
        "case-insensitive CREATE VIEW"           | "create view [DBO].[MyView] AS SELECT 1"                           | "DBO"      | "create view MyView AS SELECT 1"
        "preserves body schema references"       | "CREATE VIEW [sales].[VW] AS SELECT * FROM [hr].[EMPLOYEES]"       | "sales"    | "CREATE VIEW VW AS SELECT * FROM [hr].[EMPLOYEES]"
    }

    def "findDifferences reports no diff when definitions differ only by schema qualification"() {
        given:
        def database = new MSSQLDatabase()
        def view1 = createView("dbo", "view_demo",
                "CREATE VIEW [dbo].[view_demo] WITH SCHEMABINDING AS SELECT col1 FROM dbo.table_demo")
        def view2 = createView("dbo", "view_demo",
                "CREATE VIEW view_demo WITH SCHEMABINDING AS SELECT col1 FROM dbo.table_demo")

        def comparators = DatabaseObjectComparatorFactory.instance.getComparators(View, database)
        def chain = new DatabaseObjectComparatorChain(comparators, new CompareControl.SchemaComparison[0])

        when:
        def differences = comparator.findDifferences(
                view1, view2, database, new CompareControl(), chain, new HashSet<String>())

        then:
        !differences.isDifferent("definition")
    }

    def "findDifferences reports diff when definitions are genuinely different"() {
        given:
        def database = new MSSQLDatabase()
        def view1 = createView("dbo", "view_demo",
                "CREATE VIEW [dbo].[view_demo] AS SELECT col1 FROM table_demo")
        def view2 = createView("dbo", "view_demo",
                "CREATE VIEW [dbo].[view_demo] AS SELECT col1, col2 FROM table_demo")

        def comparators = DatabaseObjectComparatorFactory.instance.getComparators(View, database)
        def chain = new DatabaseObjectComparatorChain(comparators, new CompareControl.SchemaComparison[0])

        when:
        def differences = comparator.findDifferences(
                view1, view2, database, new CompareControl(), chain, new HashSet<String>())

        then:
        differences.isDifferent("definition")
    }

    def "findDifferences handles null definition on one side"() {
        given:
        def database = new MSSQLDatabase()
        def view1 = createView("dbo", "view_demo",
                "CREATE VIEW [dbo].[view_demo] AS SELECT 1")
        def view2 = createView("dbo", "view_demo", null)

        def comparators = DatabaseObjectComparatorFactory.instance.getComparators(View, database)
        def chain = new DatabaseObjectComparatorChain(comparators, new CompareControl.SchemaComparison[0])

        when:
        def differences = comparator.findDifferences(
                view1, view2, database, new CompareControl(), chain, new HashSet<String>())

        then:
        differences.isDifferent("definition")
    }

    def "findDifferences handles both definitions null"() {
        given:
        def database = new MSSQLDatabase()
        def view1 = createView("dbo", "view_demo", null)
        def view2 = createView("dbo", "view_demo", null)

        def comparators = DatabaseObjectComparatorFactory.instance.getComparators(View, database)
        def chain = new DatabaseObjectComparatorChain(comparators, new CompareControl.SchemaComparison[0])

        when:
        def differences = comparator.findDifferences(
                view1, view2, database, new CompareControl(), chain, new HashSet<String>())

        then:
        !differences.isDifferent("definition")
    }

    def "findDifferences reports no diff for cross-schema views with bracket differences"() {
        given:
        def database = new MSSQLDatabase()
        def view1 = createView("sales", "VW_ORDER_DETAILS",
                "CREATE VIEW [sales].[VW_ORDER_DETAILS] AS SELECT * FROM [hr].[EMPLOYEES]")
        def view2 = createView("sales", "VW_ORDER_DETAILS",
                "CREATE VIEW VW_ORDER_DETAILS AS SELECT * FROM [hr].[EMPLOYEES]")

        def comparators = DatabaseObjectComparatorFactory.instance.getComparators(View, database)
        def chain = new DatabaseObjectComparatorChain(comparators, new CompareControl.SchemaComparison[0])

        when:
        def differences = comparator.findDifferences(
                view1, view2, database, new CompareControl(), chain, new HashSet<String>())

        then:
        !differences.isDifferent("definition")
    }

    private static View createView(String schemaName, String viewName, String definition) {
        View view = new View()
        view.setName(viewName)
        view.setSchema(new Schema(null, schemaName))
        if (definition != null) {
            view.setDefinition(definition)
        }
        return view
    }
}
