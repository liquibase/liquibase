package liquibase.snapshot

import liquibase.database.Database
import liquibase.exception.DatabaseException
import liquibase.structure.DatabaseObject
import liquibase.structure.core.Table
import spock.lang.Specification

class SnapshotGeneratorChainTest extends Specification {

    private def database = Stub(Database.class)
    private def snapshotControl = Stub(SnapshotControl.class)
    private def snapshotContext = Stub(DatabaseSnapshot.class) {
        getDatabase() >> database
        getSnapshotControl() >> snapshotControl
    }
    private def object = new Table()
    private def visitingGenerator = new VisitedSnapshotGenerator(object.class)
    private def badGenerator = new BadSnapshotGenerator(object.class)
    private def replacementForBadGenerator = new ReplacingSnapshotGenerator(object.class, badGenerator.class)


    def "snapshotting null yields null"() {
        given:
        database.isSystemObject(_ as DatabaseObject) >> false
        snapshotControl.shouldInclude(_ as Class<? extends DatabaseObject>) >> true

        when:
        def snapshot = new SnapshotGeneratorChain(null).snapshot(null, snapshotContext)

        then:
        snapshot == null
    }

    def "snapshotting system object yields null"() {
        given:
        def chain = new SnapshotGeneratorChain(sortedSetOf(visitingGenerator))
        database.isSystemObject(object) >> true
        snapshotControl.shouldInclude(object.class) >> true

        when:
        def snapshot = chain.snapshot(object, snapshotContext)

        then:
        snapshot == null
    }

    def "snapshotting excluded object yields null"() {
        given:
        def chain = new SnapshotGeneratorChain(sortedSetOf(visitingGenerator))
        def object = new Table()
        database.isSystemObject(object) >> false
        snapshotControl.shouldInclude(object.class) >> false

        when:
        def snapshot = chain.snapshot(object, snapshotContext)

        then:
        snapshot == null
    }

    def "snapshotting with null generators yields null"() {
        given:
        def chain = new SnapshotGeneratorChain(null)
        def object = new Table()
        database.isSystemObject(object) >> false
        snapshotControl.shouldInclude(object.class) >> true

        when:
        def snapshot = chain.snapshot(object, snapshotContext)

        then:
        snapshot == null
    }

    def "snapshotting delegates to generators"() {
        given:
        def chain = new SnapshotGeneratorChain(sortedSetOf(visitingGenerator))
        def object = new Table()
        database.isSystemObject(object) >> false
        snapshotControl.shouldInclude(object.class) >> true
        def expectedTable = new Table()
        expectedTable.setAttribute("visited", true)


        when:
        def snapshot = chain.snapshot(object, snapshotContext)

        then:
        snapshot.getAttribute("visited", Boolean.class) == expectedTable.getAttribute("visited", Boolean.class)
    }

    def "snapshotting fails if subsequent generator returns a different instance"() {
        given:
        def chain = new SnapshotGeneratorChain(sortedSetOf(visitingGenerator, badGenerator))
        def object = new Table()
        database.isSystemObject(object) >> false
        snapshotControl.shouldInclude(object.class) >> true
        def expectedTable = new Table()
        expectedTable.setAttribute("visited", true)


        when:
        chain.snapshot(object, snapshotContext)

        then:
        def exception = thrown(DatabaseException)
        exception.message.startsWith("Snapshot generator liquibase.snapshot.BadSnapshotGenerator has returned a different reference from the previous generator liquibase.snapshot.VisitedSnapshotGenerator.")
    }

    def "snapshotting works even if first generator returns a different instance"() {
        given:
        def chain = new SnapshotGeneratorChain(sortedSetOf(badGenerator))
        def object = new Table()
        database.isSystemObject(object) >> false
        snapshotControl.shouldInclude(object.class) >> true
        def expectedTable = new Table()
        expectedTable.setAttribute("visited", true)


        when:
        def result = chain.snapshot(object, snapshotContext)

        then:
        result != null
    }

    def "snapshotting works if bad generator is replaced in the chain"() {
        given:
        def chain = new SnapshotGeneratorChain(sortedSetOf(visitingGenerator, badGenerator, replacementForBadGenerator))
        def object = new Table()
        database.isSystemObject(object) >> false
        snapshotControl.shouldInclude(object.class) >> true
        def expectedTable = new Table()
        expectedTable.setAttribute("visited", true)
        expectedTable.setAttribute("replacement", "done")


        when:
        def snapshot = chain.snapshot(object, snapshotContext)

        then:
        snapshot.getAttribute("visited", Boolean.class) == expectedTable.getAttribute("visited", Boolean.class)
        snapshot.getAttribute("replacement", String.class) == expectedTable.getAttribute("replacement", String.class)
    }

    private static SortedSet<SnapshotGenerator> sortedSetOf(SnapshotGenerator... generators) {
        def result = new TreeSet<SnapshotGenerator>()
        result.addAll(generators)
        result
    }
}

class VisitedSnapshotGenerator implements SnapshotGenerator, Comparable<SnapshotGenerator> {

    private final Class<? extends DatabaseObject> acceptedType

    VisitedSnapshotGenerator(Class<? extends DatabaseObject> acceptedType) {
        this.acceptedType = acceptedType
    }

    @Override
    int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (objectType == acceptedType) {
            return PRIORITY_DATABASE
        }
        return PRIORITY_NONE
    }

    @Override
    <T extends DatabaseObject> T snapshot(T example, DatabaseSnapshot snapshot, SnapshotGeneratorChain chain) throws DatabaseException, InvalidExampleException {
        example.setAttribute("visited", true)
        return example
    }

    @Override
    Class<? extends DatabaseObject>[] addsTo() {
        return [acceptedType]
    }

    @Override
    Class<? extends SnapshotGenerator>[] replaces() {
        return new Class[0]
    }

    @Override
    int compareTo(SnapshotGenerator o) {
        if (System.identityHashCode(this) == System.identityHashCode(o)) {
            return 0
        }
        return 1 // smaller than == most priority
    }
}

class ReplacingSnapshotGenerator implements SnapshotGenerator, Comparable<SnapshotGenerator> {

    private final Class<? extends SnapshotGenerator> replacedGenerator
    private final Class<? extends DatabaseObject> acceptedType

    ReplacingSnapshotGenerator(Class<? extends DatabaseObject> acceptedType,
                               Class<? extends SnapshotGenerator> replacedGenerator) {

        this.acceptedType = acceptedType
        this.replacedGenerator = replacedGenerator
    }

    @Override
    int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (objectType == acceptedType) {
            return PRIORITY_DATABASE
        }
        return PRIORITY_NONE
    }

    @Override
    <T extends DatabaseObject> T snapshot(T example, DatabaseSnapshot snapshot, SnapshotGeneratorChain chain) throws DatabaseException, InvalidExampleException {
        example.setAttribute("replacement", "done")
        return example
    }

    @Override
    Class<? extends DatabaseObject>[] addsTo() {
        return [acceptedType]
    }

    @Override
    Class<? extends SnapshotGenerator>[] replaces() {
        return [replacedGenerator]
    }

    @Override
    int compareTo(SnapshotGenerator o) {
        if (System.identityHashCode(this) == System.identityHashCode(o)) {
            return 0
        }
        return 1 // greater than == least priority
    }
}

class BadSnapshotGenerator implements SnapshotGenerator, Comparable<SnapshotGenerator> {

    private final Class<? extends DatabaseObject> acceptedType

    BadSnapshotGenerator(Class<? extends DatabaseObject> acceptedType) {
        this.acceptedType = acceptedType
    }

    @Override
    int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (objectType == acceptedType) {
            return PRIORITY_DATABASE
        }
        return PRIORITY_NONE
    }

    @Override
    <T extends DatabaseObject> T snapshot(T example, DatabaseSnapshot snapshot, SnapshotGeneratorChain chain) throws DatabaseException, InvalidExampleException {
        // generators are expected to add nested attributes, ... to the provided example or delegate if the example type does not match
        // they are NOT expected to create new instances of the same type
        return acceptedType.newInstance() as T
    }

    @Override
    Class<? extends DatabaseObject>[] addsTo() {
        return [acceptedType]
    }

    @Override
    Class<? extends SnapshotGenerator>[] replaces() {
        return new Class[0]
    }

    @Override
    int compareTo(SnapshotGenerator o) {
        if (System.identityHashCode(this) == System.identityHashCode(o)) {
            return 0
        }
        return 1 // greater than == least priority
    }
}
