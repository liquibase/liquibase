package liquibase.sdk.standardtest.change

import liquibase.CatalogAndSchema
import liquibase.change.Change
import liquibase.change.ChangeFactory
import liquibase.changelog.ChangeSet
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.OfflineConnection
import liquibase.sdk.supplier.change.ChangeSupplierFactory
import liquibase.sdk.supplier.database.DatabaseSupplier
import liquibase.sdk.supplier.resource.ResourceSupplier
import liquibase.sdk.verifytest.TestPermutation
import liquibase.serializer.core.string.StringChangeLogSerializer
import liquibase.sqlgenerator.SqlGeneratorFactory
import org.junit.Rule
import org.junit.rules.TestName
import spock.lang.*

class StandardChangeSpec extends Specification {

    @Shared changeSupplier = new ChangeSupplierFactory()
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

    @Unroll("valid properties for #changeClass.name are valid sql against #database.shortName")
    def "valid properties are valid sql" () {
        expect:
        for (Change change in changeSupplier.getSupplier(changeClass).getAllParameterPermutations(database)) {
            def changeSet = new ChangeSet("1", "StandardChangeSpec-test", false, false, "com/example/dbchangelog.xml", null, null, null)
            change.setChangeSet(changeSet)
            def permutation = new TestPermutation(testName.methodName)
            permutation.describe("Database", database.shortName);
            permutation.describe("Change Class", change.class);
            permutation.describe("Change", change)
            permutation.describe("Change Parameters", ChangeFactory.instance.getParameters(change))


            permutation.canVerify = database.connection != null && !(database.connection instanceof OfflineConnection)

            permutation.addSetup({
                change.resourceAccessor = resourceSupplier.simpleResourceAccessor
                return null
            } as TestPermutation.Setup)

            permutation.addAssertion({
                if (!change.supports(database)) return "DATABASE NOT SUPPORTED"
                if (change.generateStatementsVolatile(database)) return "CHANGE SQL IS VOLATILE"

                def validationErrors = change.validate(database)
                if (validationErrors.requiredErrorMessages.size() > 0) {
                    return "Missing required parameters"
                }
                if (validationErrors.unsupportedErrorMessages.size() > 0) {
                    return "Used unsupported parameters"
                }
                if (validationErrors.hasErrors()) {
                    return "Change has errors: #validationErrors.errorMessages"
                }
                return null
            } as TestPermutation.Setup)

            permutation.addSetup({
                permutation.data("sql", SqlGeneratorFactory.instance.generateSql(change, database as Database))
                return null
            } as TestPermutation.Setup)

            permutation.addVerification( {
                changeSupplier.prepareDatabase(change, database)
                database.executeStatements(change, null, null)
            } as TestPermutation.Verification)

            permutation.addCleanup( {
                changeSupplier.revertDatabase(change, database)
                database.dropDatabaseObjects(CatalogAndSchema.DEFAULT);
            } as TestPermutation.Cleanup)

            permutation.test()
        }


        where:
        [changeClass, database] << [changeSupplier.extensionClasses, databaseSupplier.allDatabases].combinations()
    }

//    @Unroll("Minimum required properties for <#change> is valid sql on #database.shortName")
//    def "minimum required properties is valid sql" () {
//        setup:
//        def permutation = new TestPermutation(testName.methodName)
//        permutation.describe("Database", database.shortName);
//        permutation.describe("Change Class", change.class);
//
//
//        if (!change.supports(database)) permutation.skipMessage = "DATABASE NOT SUPPORTED"
//        if (change.generateStatementsVolatile(database)) permutation.skipMessage = "CHANGE SQL IS VOLATILE"
//        permutation.canVerify = database.connection != null && !(database.connection instanceof OfflineConnection)
//
//        permutation.addSetup({
//            def changeMetaData = ChangeFactory.getInstance().getChangeMetaData(change)
//            for (paramName in changeMetaData.getRequiredParameters(database).keySet()) {
//                def param = changeMetaData.parameters.get(paramName)
//                def exampleValue = param.getExampleValue(database)
//
//                permutation.note("Change Parameter", exampleValue)
//                param.setValue(change, exampleValue)
//            }
//
//            change.resourceAccessor = resourceSupplier.simpleResourceAccessor
//        } as TestPermutation.Setup)
//
//        permutation.addSetup({
//            permutation.data("sql", SqlGeneratorFactory.instance.generateSql(change, database))
//        } as TestPermutation.Setup)
//
//        permutation.addAssertion( {assert !change.validate(database).hasErrors(), "Change has errors: ${change.validate(database).errorMessages}"} as TestPermutation.Assertion)
//
//        permutation.addVerification( {
//            changeSupplier.prepareDatabase(change, database)
//            database.executeStatements(change, null, null)
//        } as TestPermutation.Verification)
//
//        permutation.addCleanup( {
//            changeSupplier.revertDatabase(change, database)
//            database.dropDatabaseObjects(CatalogAndSchema.DEFAULT);
//        } as TestPermutation.Cleanup)
//
//        expect:
//        permutation.test()
//
//
//
//        where:
//        [change, database] << [changeSupplier.extensionChanges, databaseSupplier.allDatabases].combinations()
//    }

}
