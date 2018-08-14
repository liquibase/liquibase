package liquibase.change

import liquibase.Scope
import liquibase.sdk.supplier.resource.ResourceSupplier
import liquibase.snapshot.SnapshotGeneratorFactory
import spock.lang.Shared
import spock.lang.Specification

/**
 * Base test class for changes
 */
public abstract class StandardChangeTest extends Specification {

//    @Shared changeSupplier = new ChangeSupplierFactory()
    @Shared resourceSupplier = new ResourceSupplier()

    def cleanup() {
        SnapshotGeneratorFactory.reset()
    }

    def "refactoring name matches expected class name"() {
        expect:
        assert Scope.getCurrentScope().getSingleton(ChangeFactory.class).getChangeMetaData(getChangeClass().newInstance()).getName().toLowerCase() == getExpectedChangeName()
    }

    protected String getExpectedChangeName() {
        getChangeClass().getSimpleName().replaceFirst('Change$', "").toLowerCase()
    }

//    def "generateChecksum produces different values with each field"() {
//        given:
//        if (!canUseStandardGenerateCheckSumTest()) {
//            return;
//        }
//        def changeClass = getChangeClass()
//
//        expect:
//        Map<String, String> seenCheckSums = new HashMap<String, String>();
//
//        def database = new MockDatabase()
//        for (Change change in changeSupplier.getSupplier(changeClass).getAllParameterPermutations(database)) {
//            change.setResourceAccessor(resourceSupplier.simpleResourceAccessor)
//            change.setChangeSet(new ChangeSet("mock", "test", false, false, null, null, null, null))
//            if (change.validate(database).hasErrors()) {
//                continue
//            }
//            def checkSum = change.generateCheckSum()
//            assert CheckSum.getCurrentVersion() == checkSum.getVersion();
//            assert checkSum.toString().startsWith(CheckSum.getCurrentVersion()+":")
//
//            def serialized = new StringChangeLogSerializer().serialize(change, false);
//
//            if (seenCheckSums.containsKey(checkSum.toString())) {
//                if (!serialized.equals(seenCheckSums.get(checkSum.toString()))) {
//                    fail "generated duplicate checksum: "+serialized+" matches "+seenCheckSums.get(checkSum.toString())
//                }
//            }
//
//            seenCheckSums.put(checkSum.toString(), serialized);
//
//        }
//        assert seenCheckSums.size() > 0 : "No changes found to check checksums for"
//    }

//    @Unroll()
//    def "parse and load work together"() {
//        expect:
//        def serialized = change.serialize()
//        assert serialized != null
//
//        def newChange = changeClass.newInstance() as Change
//        if (!isValidForLoad(change)) {
//            return;
//        }
//        newChange.load(serialized, resourceSupplier.simpleResourceAccessor)
//        def reserialized = newChange.serialize()
//
//        serialized.toString().replaceAll("\\.0(\\D)","\$1") == reserialized.toString().replaceAll("\\.0(\\D)","\$1") //have to replace .0's because of int->decimal conversions
//
//        where:
//        change << changeSupplier.getSupplier(changeClass).getAllParameterPermutations(new MockDatabase())
//    }

    def isValidForLoad(Change change) {
        return true;
    }

    protected boolean canUseStandardGenerateCheckSumTest() {
        return true;
    }

    protected Class getChangeClass() {
        Class.forName(getClass().getName().replaceAll('Test$', ""))
    }
}
