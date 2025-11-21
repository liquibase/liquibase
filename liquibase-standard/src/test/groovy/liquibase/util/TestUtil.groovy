package liquibase.util

import groovy.transform.CompileStatic
import liquibase.ExtensibleObject
import liquibase.Scope
import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.changelog.RanChangeSet
import liquibase.database.Database
import liquibase.database.core.MockDatabase
import liquibase.parser.core.ParsedNode
import liquibase.resource.ResourceAccessor
import liquibase.serializer.LiquibaseSerializable

import java.lang.reflect.Modifier

import static org.junit.Assert.assertNotNull

/**
 * Test-centric utility methods
 */
@CompileStatic
abstract class TestUtil {

    static Map<Class, SortedSet<Class>> allClasses

    /**
     * Creates all permutations of a given class without adding additional null values
     */
    static <T extends ExtensibleObject> List<T> createAllPermutationsWithoutNulls(Class<T> type, Map<String, List<Object>> defaultValues) throws Exception {
        return createAllPermutations(type, defaultValues, false)
    }

    static <T extends ExtensibleObject> List<T> createAllPermutations(Class<T> type, Map<String, List<Object>> defaultValues, boolean addNulls = true) throws Exception {
        List<T> returnList = new ArrayList<>();
        for (Map<String, Object> parameterValues : CollectionUtil.permutations(defaultValues)) {
            T obj = type.getConstructor().newInstance();
            for (Map.Entry<String, ?> entry : parameterValues.entrySet()) {
                if (obj.getObjectMetaData().getAttribute(entry.getKey()) == null) {
                    throw new RuntimeException("No attribute "+entry.getKey()+" on "+type.getName())
                }
                obj.set(entry.getKey(), entry.getValue());
            }
            returnList.add(obj);
        }

        return returnList;
    }


    static SortedSet<Class> getClasses(Class baseClass) {
        if (allClasses == null) {
            allClasses = [:]
            findAllClasses()
        }

        return allClasses.get(baseClass)
    }


    private static findAllClasses() {
        def workingDir = new File(".")

        def startDir = new File(workingDir, "target/classes")
        startDir.traverse {
            if (!it.isFile()) {
                return
            }

            def file = it
            if (file.name.endsWith(".class")) {
                if (file.name.contains('$_$') || file.name.find(/_closure\d+/)) { // a groovy closure
                    return;
                }
                if (file.name.contains("Abstract")) {
                    return;
                }
                if (file.name.endsWith("package-info.class")) {
                    return;
                }

                def className = file.absolutePath.replace(startDir.absolutePath, "")
                        .replace("\\", ".")
                        .replace("/", ".")
                        .replaceFirst(/\.class$/, "")
                        .replaceFirst(/^\./, "")
                Class clazz = Class.forName(className);

                if (!isValidClass(clazz)) {
                    return;
                }

                Class superClass = clazz.superclass;
                Set<Class> interfaces = createClassSortedSet()
                addInterfaces(clazz, interfaces)

                while (superClass != null && !superClass.equals(Object.class)) {
                    def classList = allClasses.get(superClass)
                    if (classList == null) {
                        classList = createClassSortedSet();
                        allClasses.put(superClass, classList)
                    }
                    allClasses.get(superClass).add(clazz)
                    addInterfaces(superClass, interfaces)

                    superClass = superClass.superclass
                }

                for (def iface : interfaces) {
                    def classList = allClasses.get(iface)
                    if (classList == null) {
                        classList = createClassSortedSet();
                        allClasses.put(iface, classList)
                    }
                    classList.add(clazz);
                }
            }
        }
    }

    private static TreeSet createClassSortedSet() {
        new TreeSet<>(new Comparator<Class>() {
            @Override
            int compare(Class o1, Class o2) {
                return o1.getName().compareTo(o2.getName())
            }
        })
    }

    private static addInterfaces(Class<?> clazz, Set<Class> interfaces) {
        def thisInterfaces = clazz.interfaces
        if (thisInterfaces.size() > 0) {
            for (Class iface : thisInterfaces) {
                interfaces.add(iface)
                addInterfaces(iface, interfaces)
            }
        }

    }

    private static isValidClass(Class<?> clazz) {
        try {
            if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers()) || clazz.isSynthetic() || clazz.isAnonymousClass()) {
                return false;
            }
        } catch (NoClassDefFoundError e) {
            println "Error with "+clazz.name
            throw e
        }

        return true;
    }

    static void assertAllDeploymentIdsNonNull() {
        def database = Scope.getCurrentScope().get("database", new MockDatabase()) as Database
        def changelogHistoryService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database)
        List<RanChangeSet> ranChangeSets = changelogHistoryService.getRanChangeSets()
        for (RanChangeSet ranChangeSet : ranChangeSets) {
            assertNotNull(ranChangeSet.getDeploymentId())
        }
    }

    /** Simple load a change providing the change instance, and using named parameters as children
     <code>load( new StopChange(), message: "out text", testResourceAcccessor);</code>
     *
     * @param children named method parameters collected by the compiler or actual map of children, ParsedNode can process
     * @param change the change to load from {@link ParsedNode}
     * @param resourceAccessor optional ResourceAccessor the change requires
     * @return the {@code change} instance with children loaded using ParsedNode
     */
    static <T extends LiquibaseSerializable> T load(Map children, T change, ResourceAccessor resourceAccessor = null) {
        change.load( parsedNode( children, change.serializedObjectName ), resourceAccessor)
        change
    }

    /** Create a {@link ParsedNode} instance with the given {@code name} and {@code children} */
    static ParsedNode parsedNode(Map<String, Object> children, String name = "") {
        new ParsedNode(null, name).setValue(children)
    }
}
