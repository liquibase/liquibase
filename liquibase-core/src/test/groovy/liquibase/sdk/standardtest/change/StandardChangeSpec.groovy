package liquibase.sdk.standardtest.change

import liquibase.change.ChangeFactory
import liquibase.database.DatabaseFactory
import liquibase.sdk.supplier.change.ChangeSupplier
import liquibase.sdk.supplier.database.DatabaseSupplier
import liquibase.sdk.supplier.resource.ResourceSupplier
import liquibase.sdk.verifytest.TestPermutation
import liquibase.sqlgenerator.SqlGeneratorFactory
import org.junit.Rule
import org.junit.rules.TestName
import spock.lang.*

class StandardChangeSpec extends Specification {

    @Shared changeSupplier = new ChangeSupplier()
    @Shared databaseSupplier = new DatabaseSupplier()
    @Shared resourceSupplier = new ResourceSupplier()
    @Rule TestName testName = new TestName()

    @Unroll("Change <#change> has at least one supported database")
    def "change class has at least one supported database" () {
        expect:
        for (database in DatabaseFactory.instance.implementedDatabases) {
            if (change.supports(database)) {
                return
            }
        }
        assert false, "No supported database"

        where:
        change << changeSupplier.extensionChanges
    }

    @Unroll("Minimum required properties for <#change> is valid sql on #database.shortName")
    def "minimum required properties is valid sql" () {
        setup:
        def permutation = new TestPermutation(testName.methodName)
        permutation.describe("Database", database.shortName);
        permutation.describe("Change Class", change.class);


        if (!change.supports(database)) permutation.skipMessage = "DATABASE NOT SUPPORTED"
        if (change.generateStatementsVolatile(database)) permutation.skipMessage = "CHANGE SQL IS VOLATILE"

        permutation.addSetup({
            def changeMetaData = ChangeFactory.getInstance().getChangeMetaData(change)
            for (paramName in changeMetaData.getRequiredParameters(database).keySet()) {
                def param = changeMetaData.parameters.get(paramName)
                def exampleValue = param.exampleValue

                permutation.note("Change Parameter", exampleValue)
                param.setValue(change, exampleValue)
            }

            change.resourceAccessor = resourceSupplier.simpleResourceAccessor
        } as TestPermutation.Setup)

        permutation.addSetup({
            permutation.data("sql", SqlGeneratorFactory.instance.generateSql(change, database))
        } as TestPermutation.Setup)

        permutation.addAssertion( {assert !change.validate(database).hasErrors(), "Change has errors: ${change.validate(database).errorMessages}"} as TestPermutation.Assertion)

        permutation.addVerification( {
            if (database.connection == null) throw new TestPermutation.CannotVerifyException("No database connection")

            database.executeStatements(change, null, null)
        } as TestPermutation.Verification)

        expect:
        permutation.test()



        where:
        [change, database] << [changeSupplier.extensionChanges, databaseSupplier.allDatabases].combinations()
    }

}
